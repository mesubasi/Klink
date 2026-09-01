package com.urlshortener.controller;

import com.urlshortener.dto.ApiKeyActionRequest;
import com.urlshortener.dto.ApiKeyApplyRequest;
import com.urlshortener.dto.ApiKeyResponse;
import com.urlshortener.model.ApiKeyStatus;
import com.urlshortener.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Developer API Keys", description = "Geliştirici API Anahtarı başvuru, onay ve yönetim uçları")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    // User Endpoints
    @PostMapping("/api-keys/apply")
    @Operation(summary = "Yeni API Anahtarı Başvurusu Yap", description = "Kullanıcı uygulama adı, kullanım amacı ve web sitesini belirterek API anahtarı talebinde bulunur")
    public ResponseEntity<ApiKeyResponse> applyForApiKey(@RequestBody ApiKeyApplyRequest request) {
        ApiKeyResponse response = apiKeyService.applyForApiKey(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api-keys/me")
    @Operation(summary = "Kullanıcının API Anahtarlarını ve Başvurularını Getir")
    public ResponseEntity<List<ApiKeyResponse>> getMyApiKeys() {
        List<ApiKeyResponse> list = apiKeyService.getMyApiKeys();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/api-keys/{id}/regenerate")
    @Operation(summary = "Aktif API Anahtarını Yenile", description = "Mevcut anahtarı iptal edip yeni bir güvenli anahtar üretir")
    public ResponseEntity<ApiKeyResponse> regenerateApiKey(@PathVariable UUID id) {
        ApiKeyResponse response = apiKeyService.regenerateApiKey(id);
        return ResponseEntity.ok(response);
    }

    // Admin Endpoints
    @GetMapping("/admin/api-keys")
    @Operation(summary = "Admin: Tüm API Başvurularını Listele", description = "Opsiyonel olarak ?status=PENDING/APPROVED/REJECTED/REVOKED filtresi alır")
    public ResponseEntity<List<ApiKeyResponse>> getAllApplications(@RequestParam(required = false) ApiKeyStatus status) {
        List<ApiKeyResponse> list = apiKeyService.getAllApplications(status);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/admin/api-keys/{id}/approve")
    @Operation(summary = "Admin: API Başvurusunu Onayla", description = "Başvuruyu onaylayarak kl_live_ formatında anahtar üretir ve hız limiti tanımlar")
    public ResponseEntity<ApiKeyResponse> approveApplication(@PathVariable UUID id, @RequestBody(required = false) ApiKeyActionRequest request) {
        ApiKeyResponse response = apiKeyService.approveApplication(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/api-keys/{id}/reject")
    @Operation(summary = "Admin: API Başvurusunu Reddet", description = "Reddedilme gerekçesi ile birlikte başvuruyu reddeder")
    public ResponseEntity<ApiKeyResponse> rejectApplication(@PathVariable UUID id, @RequestBody(required = false) ApiKeyActionRequest request) {
        ApiKeyResponse response = apiKeyService.rejectApplication(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/api-keys/{id}/revoke")
    @Operation(summary = "Admin: API Anahtarını İptal Et", description = "Kötüye kullanılan veya süresi dolan anahtarı askıya alır/iptal eder")
    public ResponseEntity<ApiKeyResponse> revokeApiKey(@PathVariable UUID id) {
        ApiKeyResponse response = apiKeyService.revokeApiKey(id);
        return ResponseEntity.ok(response);
    }
}
