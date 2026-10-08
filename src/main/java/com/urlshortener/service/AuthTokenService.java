package com.urlshortener.service;

import com.urlshortener.model.AuditAction;
import com.urlshortener.model.AuthToken;
import com.urlshortener.model.AuthTokenType;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.AuthTokenRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Email verification and password reset through single-use, expiring links. */
@Service
public class AuthTokenService {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenService.class);
    private static final String INVALID_LINK = "Bağlantı geçersiz veya süresi dolmuş.";
    private static final long RESEND_COOLDOWN_MS = TimeUnit.SECONDS.toMillis(60);
    private static final long VERIFICATION_TTL_MS = TimeUnit.HOURS.toMillis(24);
    private static final long RESET_TTL_MS = TimeUnit.HOURS.toMillis(1);

    private final AuthTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditService auditService;

    @Value("${app.frontend.verify-url:http://localhost:3000/verify-email/%s}")
    private String verifyUrlPattern;

    @Value("${app.frontend.reset-url:http://localhost:3000/reset-password/%s}")
    private String resetUrlPattern;

    public AuthTokenService(AuthTokenRepository tokenRepository, UserRepository userRepository,
                            PasswordEncoder passwordEncoder, EmailService emailService, AuditService auditService) {
        this.auditService = auditService;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /** Sends a fresh verification link. Returns false (and sends nothing) when one was issued less than a minute ago. */
    @Transactional
    public boolean sendEmailVerification(UserAccount user) {
        if (user.isEmailVerified()) {
            return false;
        }
        if (issuedRecently(user, AuthTokenType.EMAIL_VERIFICATION)) {
            return false;
        }
        String token = issue(user, AuthTokenType.EMAIL_VERIFICATION, VERIFICATION_TTL_MS);
        emailService.sendEmailVerification(user.getEmail(), user.getUsername(), String.format(verifyUrlPattern, token));
        return true;
    }

    @Transactional
    public void verifyEmail(String token) {
        AuthToken stored = redeemable(token, AuthTokenType.EMAIL_VERIFICATION);
        UserAccount user = stored.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        stored.setUsedAt(System.currentTimeMillis());
        tokenRepository.save(stored);
        auditService.recordAs(user.getUsername(), user.getRole(), AuditAction.EMAIL_VERIFIED, AuditService.SUCCESS, "USER", user.getUsername(), null, null);
        log.info("E-posta doğrulandı: {}", user.getUsername());
    }

    /**
     * Starts a password reset. Never reveals whether the email is registered: callers always answer the same way.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        Optional<UserAccount> found = userRepository.findByEmail(email.trim().toLowerCase());
        if (found.isEmpty()) {
            log.info("Parola sıfırlama istendi ancak kayıtlı e-posta bulunamadı.");
            return;
        }
        UserAccount user = found.get();
        if (issuedRecently(user, AuthTokenType.PASSWORD_RESET)) {
            return;
        }
        String token = issue(user, AuthTokenType.PASSWORD_RESET, RESET_TTL_MS);
        auditService.recordAs(user.getUsername(), user.getRole(), AuditAction.PASSWORD_RESET_REQUESTED, AuditService.SUCCESS, "USER", user.getUsername(), null, null);
        emailService.sendPasswordReset(user.getEmail(), user.getUsername(), String.format(resetUrlPattern, token));
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        AuthToken stored = redeemable(token, AuthTokenType.PASSWORD_RESET);
        UserAccount user = stored.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        // Receiving the link proves control of the mailbox.
        user.setEmailVerified(true);
        // Anyone holding a token from before the reset (e.g. an attacker who had the old password) is signed out.
        user.revokeAllTokens();
        userRepository.save(user);

        long now = System.currentTimeMillis();
        stored.setUsedAt(now);
        tokenRepository.save(stored);
        tokenRepository.invalidateOpenTokens(user.getId(), AuthTokenType.PASSWORD_RESET, now);
        auditService.recordAs(user.getUsername(), user.getRole(), AuditAction.PASSWORD_RESET_COMPLETED, AuditService.SUCCESS, "USER", user.getUsername(), null, "all earlier sessions revoked");
        log.info("Parola sıfırlandı: {}", user.getUsername());
    }

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000L)
    @Transactional
    public void purgeExpiredTokens() {
        int removed = tokenRepository.deleteExpiredBefore(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));
        if (removed > 0) {
            log.info("{} süresi dolmuş kimlik doğrulama token'ı temizlendi.", removed);
        }
    }

    private AuthToken redeemable(String token, AuthTokenType type) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(INVALID_LINK);
        }
        return tokenRepository.findForUpdate(TokenUtil.hash(token), type)
                .filter(AuthToken::isUsable)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_LINK));
    }

    private boolean issuedRecently(UserAccount user, AuthTokenType type) {
        return tokenRepository.findFirstByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), type)
                .map(t -> System.currentTimeMillis() - t.getCreatedAt() < RESEND_COOLDOWN_MS)
                .orElse(false);
    }

    private String issue(UserAccount user, AuthTokenType type, long ttlMs) {
        long now = System.currentTimeMillis();
        tokenRepository.invalidateOpenTokens(user.getId(), type, now);

        String token = TokenUtil.generate();
        AuthToken entity = new AuthToken();
        entity.setUser(user);
        entity.setType(type);
        entity.setTokenHash(TokenUtil.hash(token));
        entity.setCreatedAt(now);
        entity.setExpiresAt(now + ttlMs);
        tokenRepository.save(entity);
        return token;
    }
}
