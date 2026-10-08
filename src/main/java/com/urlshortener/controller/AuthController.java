package com.urlshortener.controller;

import com.urlshortener.dto.*;
import com.urlshortener.service.ActionRateLimiter;
import com.urlshortener.service.AuthService;
import com.urlshortener.service.AuthTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Kimlik ve Üyelik İşlemleri", description = "Kullanıcı kaydı, giriş, 2FA güvenlik işlemleri, profil ve oturum kapatma servisleri")
public class AuthController {

    private final AuthService authService;
    private final AuthTokenService authTokenService;
    private final ActionRateLimiter rateLimiter;

    public AuthController(AuthService authService, AuthTokenService authTokenService, ActionRateLimiter rateLimiter) {
        this.authService = authService;
        this.authTokenService = authTokenService;
        this.rateLimiter = rateLimiter;
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/login")
    @Operation(summary = "Kullanıcı Girişi (Login)", description = "Kullanıcı adı ve şifre ile giriş yapar. 2FA aktifse twoFactorRequired: true döner.")
    @ApiResponse(responseCode = "200", description = "Giriş başarılı veya 2FA kodu bekleniyor")
    @ApiResponse(responseCode = "401", description = "Hatalı kullanıcı adı veya şifre")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/verify-login")
    @Operation(summary = "2FA Giriş Doğrulama", description = "2FA aktif hesaplar için kullanıcı adı, şifre ve 6 haneli TOTP kodunu doğrular.")
    @ApiResponse(responseCode = "200", description = "2FA doğrulaması başarılı, JWT token döndürüldü")
    @ApiResponse(responseCode = "400", description = "Geçersiz 2FA kodu veya kimlik bilgileri")
    public ResponseEntity<AuthResponse> verify2FALogin(@Valid @RequestBody TotpLoginRequest request) {
        AuthResponse response = authService.verify2FALogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/setup")
    @Operation(summary = "2FA Kurulumu", description = "Giriş yapmış kullanıcı için QR Kod (Base64) ve Gizli Anahtar üretir.")
    @ApiResponse(responseCode = "200", description = "2FA kurulum detayları oluşturuldu")
    public ResponseEntity<TotpSetupResponse> setup2FA(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TotpSetupResponse response = authService.setup2FA(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/enable")
    @Operation(summary = "2FA Aktifleştirme", description = "Authenticator uygulamasından alınan 6 haneli kod doğrulandığında hesaba 2FA tanımlar.")
    @ApiResponse(responseCode = "200", description = "2FA başarıyla aktifleştirildi")
    public ResponseEntity<AuthResponse> enable2FA(@Valid @RequestBody TotpVerifyRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse response = authService.enable2FA(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/disable")
    @Operation(summary = "2FA Devre Dışı Bırakma", description = "Doğrulama kodu girildiğinde hesaptaki 2FA korumasını kaldırır.")
    @ApiResponse(responseCode = "200", description = "2FA başarıyla devre dışı bırakıldı")
    public ResponseEntity<AuthResponse> disable2FA(@Valid @RequestBody TotpVerifyRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse response = authService.disable2FA(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Yeni Kullanıcı Kaydı (Üye Ol)", description = "Sisteme yeni bir kullanıcı hesabı açar ve JWT token döner.")
    @ApiResponse(responseCode = "201", description = "Kullanıcı kaydı başarıyla oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Kullanıcı adı veya e-posta adresi zaten kullanımda")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        rateLimiter.check("register", clientIp(httpRequest), 10, java.time.Duration.ofHours(1));
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "E-posta Adresini Doğrula", description = "Doğrulama e-postasındaki tek kullanımlık anahtarla e-posta adresini doğrular.")
    public ResponseEntity<java.util.Map<String, String>> verifyEmail(@Valid @RequestBody TokenRequest request, HttpServletRequest httpRequest) {
        rateLimiter.check("verify-email", clientIp(httpRequest), 30, java.time.Duration.ofHours(1));
        authTokenService.verifyEmail(request.getToken());
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "E-posta adresiniz doğrulandı."));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Doğrulama E-postasını Yeniden Gönder", description = "Giriş yapmış kullanıcıya yeni bir doğrulama bağlantısı gönderir (dakikada en fazla bir kez).")
    public ResponseEntity<java.util.Map<String, String>> resendVerification(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        rateLimiter.check("resend-verification", authentication.getName(), 5, java.time.Duration.ofHours(1));
        boolean sent = authService.resendEmailVerification(authentication.getName());
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", sent
                ? "Doğrulama e-postası gönderildi. Gelen kutunuzu kontrol edin."
                : "E-posta zaten doğrulanmış ya da az önce bir bağlantı gönderilmiş. Lütfen bir dakika bekleyip tekrar deneyin."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Parola Sıfırlama İste", description = "E-posta kayıtlıysa 1 saat geçerli, tek kullanımlık sıfırlama bağlantısı gönderir. E-posta kayıtlı olsun ya da olmasın aynı yanıt döner.")
    public ResponseEntity<java.util.Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        rateLimiter.check("forgot-password", clientIp(httpRequest), 5, java.time.Duration.ofHours(1));
        authTokenService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(java.util.Collections.singletonMap("message",
                "E-posta adresi kayıtlıysa parola sıfırlama bağlantısı gönderildi."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Parolayı Sıfırla", description = "E-postadaki tek kullanımlık anahtarla yeni parola belirler.")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        rateLimiter.check("reset-password", clientIp(httpRequest), 10, java.time.Duration.ofHours(1));
        authTokenService.resetPassword(request.getToken(), request.getPassword());
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Parolanız güncellendi. Yeni parolanızla giriş yapabilirsiniz."));
    }

    @GetMapping("/me")
    @Operation(summary = "Giriş Yapan Kullanıcı Profili", description = "O an oturum açmış kullanıcının kullanıcı adı, e-posta, rol ve 2FA durum bilgilerini döner.")
    @ApiResponse(responseCode = "200", description = "Kullanıcı profili getirildi")
    @ApiResponse(responseCode = "401", description = "Oturum açılmamış (Unauthorized)")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDto user = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Tüm Cihazlardaki Oturumları Kapat", description = "Bu hesap için şimdiye kadar verilmiş tüm erişim token'larını geçersiz kılar; her cihazda yeniden giriş gerekir.")
    public ResponseEntity<java.util.Map<String, String>> logoutEverywhere(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authService.logoutEverywhere(authentication.getName());
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Tüm cihazlardaki oturumlar kapatıldı. Yeniden giriş yapmanız gerekiyor."));
    }

    @PostMapping("/logout")
    @Operation(summary = "Oturumu Kapat (Logout)", description = "Oturum açmış kullanıcının oturumunu kapatır.")
    @ApiResponse(responseCode = "200", description = "Oturum başarıyla kapatıldı")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        AuthResponse authResponse = AuthResponse.builder()
                .message(authService.getLogoutMessage())
                .build();
        return ResponseEntity.ok(authResponse);
    }
}
