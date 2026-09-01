package com.urlshortener.controller;

import com.urlshortener.dto.*;
import com.urlshortener.service.AuthService;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
