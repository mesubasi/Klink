package com.urlshortener.service;

import com.urlshortener.dto.*;
import com.urlshortener.model.AuditAction;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final TotpService totpService;
    private final AuthTokenService authTokenService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MessageService messageService,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       TotpService totpService,
                       AuthTokenService authTokenService,
                       AuditService auditService) {
        this.authTokenService = authTokenService;
        this.auditService = auditService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageService = messageService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.totpService = totpService;
    }

    /** Checks the credentials; a refusal is written to the audit trail (with the attempted username, never the password). */
    private Authentication authenticateOrAudit(String username, String password) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (org.springframework.security.core.AuthenticationException e) {
            auditService.recordAs(null, null, AuditAction.LOGIN_FAILED, AuditService.FAILURE, "USER", username, null, "bad credentials");
            throw e;
        }
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticateOrAudit(request.getUsername().trim(), request.getPassword());

        UserAccount user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("user.not_found", request.getUsername())));

        if (user.isTwoFactorEnabled()) {
            return AuthResponse.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .twoFactorRequired(true)
                    .message("2FA doğrulama kodu gereklidir.")
                    .build();
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(user);
        auditService.recordAs(user.getUsername(), user.getRole(), AuditAction.LOGIN_SUCCESS, AuditService.SUCCESS, "USER", user.getUsername(), null, null);

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(token)
                .tokenType("Bearer")
                .twoFactorRequired(false)
                .message("Başarıyla giriş yapıldı.")
                .build();
    }

    public AuthResponse verify2FALogin(TotpLoginRequest request) {
        Authentication authentication = authenticateOrAudit(request.getUsername().trim(), request.getPassword());

        UserAccount user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("user.not_found", request.getUsername())));

        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            throw new IllegalArgumentException("Bu hesapta 2FA aktif değildir.");
        }

        boolean valid = totpService.verifyCode(user.getTwoFactorSecret(), request.getCode());
        if (!valid) {
            auditService.recordAs(user.getUsername(), user.getRole(), AuditAction.LOGIN_FAILED, AuditService.FAILURE, "USER", user.getUsername(), null, "invalid 2FA code");
            throw new IllegalArgumentException("Girdiğiniz 2FA kodu geçersiz veya süresi dolmuş!");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(user);
        auditService.recordAs(user.getUsername(), user.getRole(), AuditAction.LOGIN_SUCCESS, AuditService.SUCCESS, "USER", user.getUsername(), null, null);

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(token)
                .tokenType("Bearer")
                .twoFactorRequired(false)
                .message("2FA doğrulaması başarılı. Giriş yapıldı.")
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException(messageService.getMessage("user.username_exists", request.getUsername()));
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException(messageService.getMessage("user.email_exists", request.getEmail()));
        }

        UserAccount user = UserAccount.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .twoFactorEnabled(false)
                .emailVerified(false)
                .build();

        userRepository.save(user);
        authTokenService.sendEmailVerification(user);
        auditService.recordAs(user.getUsername(), user.getRole(), AuditAction.USER_REGISTERED, AuditService.SUCCESS, "USER", user.getUsername(), null, null);

        String token = tokenProvider.generateToken(user);

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(token)
                .tokenType("Bearer")
                .message(messageService.getMessage("user.register_success"))
                .build();
    }

    public TotpSetupResponse setup2FA(String username) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("user.not_found", username)));

        String secretKey = totpService.generateSecretKey();
        String otpAuthUrl = totpService.getOtpAuthUrl(user.getUsername(), secretKey);
        String qrCodeUrl = totpService.generateQrCodeBase64(otpAuthUrl);

        return new TotpSetupResponse(secretKey, qrCodeUrl, otpAuthUrl);
    }

    @Transactional
    public AuthResponse enable2FA(String username, TotpVerifyRequest request) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("user.not_found", username)));

        if (request.getSecretKey() == null || request.getSecretKey().trim().isEmpty()) {
            throw new IllegalArgumentException("2FA kurulum gizli anahtarı eksik!");
        }

        boolean valid = totpService.verifyCode(request.getSecretKey(), request.getCode());
        if (!valid) {
            throw new IllegalArgumentException("Girdiğiniz 2FA kodu geçersiz! Lütfen authenticator uygulamanızdaki 6 haneli güncel kodu girin.");
        }

        user.setTwoFactorSecret(request.getSecretKey().trim());
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        auditService.recordAs(user.getUsername(), user.getRole(), AuditAction.TWO_FACTOR_ENABLED, AuditService.SUCCESS, "USER", user.getUsername(), null, null);

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .twoFactorRequired(false)
                .message("İki aşamalı doğrulama (2FA) başarıyla aktifleştirildi!")
                .build();
    }

    @Transactional
    public AuthResponse disable2FA(String username, TotpVerifyRequest request) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("user.not_found", username)));

        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            throw new IllegalArgumentException("2FA zaten etkin değil!");
        }

        boolean valid = totpService.verifyCode(user.getTwoFactorSecret(), request.getCode());
        if (!valid) {
            throw new IllegalArgumentException("Girdiğiniz 2FA kodu geçersiz!");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        auditService.recordAs(user.getUsername(), user.getRole(), AuditAction.TWO_FACTOR_DISABLED, AuditService.SUCCESS, "USER", user.getUsername(), null, null);

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .twoFactorRequired(false)
                .message("İki aşamalı doğrulama (2FA) devre dışı bırakıldı.")
                .build();
    }

    public UserDto getCurrentUser(String username) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("user.not_found", username)));

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public boolean resendEmailVerification(String username) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("user.not_found", username)));
        return authTokenService.sendEmailVerification(user);
    }

    /** Signs the user out of every device: all tokens issued so far stop working. */
    @Transactional
    public void logoutEverywhere(String username) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("user.not_found", username)));
        user.revokeAllTokens();
        userRepository.save(user);
        auditService.record(AuditAction.LOGOUT_EVERYWHERE, "USER", user.getUsername(), null, null);
    }

    public String getLogoutMessage() {
        return messageService.getMessage("user.logout_success");
    }
}
