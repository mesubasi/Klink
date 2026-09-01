package com.urlshortener.controller;

import com.urlshortener.dto.BioPageDto;
import com.urlshortener.dto.BioPageUpdateRequest;
import com.urlshortener.service.BioPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bio")
@Tag(name = "Bio Page (Link-in-Bio)", description = "Kişiselleştirilebilir Link-in-Bio profil ve mikro açılış sayfası API'ları")
public class BioPageController {

    private final BioPageService bioPageService;

    public BioPageController(BioPageService bioPageService) {
        this.bioPageService = bioPageService;
    }

    @GetMapping("/{username}")
    @Operation(summary = "Kullanıcının Link-in-Bio Sayfasını Getir", description = "Public olarak herkesin erişebildiği Link-in-Bio micro landing sayfası verisi")
    public ResponseEntity<BioPageDto> getPublicBioPage(@PathVariable String username) {
        BioPageDto bioPage = bioPageService.getBioPageByUsername(username, false);
        return ResponseEntity.ok(bioPage);
    }

    @PostMapping("/{username}/view")
    @Operation(summary = "Bio Sayfası Görüntülenme Sayısını Artır")
    public ResponseEntity<Map<String, String>> recordBioPageView(@PathVariable String username) {
        bioPageService.getBioPageByUsername(username, true);
        return ResponseEntity.ok(Map.of("message", "View recorded"));
    }

    @PostMapping("/link/{linkId}/click")
    @Operation(summary = "Bio Linkine Tıklanma Sayısını Artır")
    public ResponseEntity<Map<String, String>> recordLinkClick(@PathVariable UUID linkId) {
        bioPageService.recordLinkClick(linkId);
        return ResponseEntity.ok(Map.of("message", "Click recorded"));
    }

    @GetMapping("/me")
    @Operation(summary = "Giriş Yapmış Kullanıcının Bio Sayfasını Getir", description = "Dashboard'da düzenlemek üzere kullanıcının kendi bio sayfasını ve tüm linklerini getirir")
    public ResponseEntity<BioPageDto> getMyBioPage() {
        BioPageDto bioPage = bioPageService.getBioPageForCurrentUser();
        return ResponseEntity.ok(bioPage);
    }

    @PutMapping("/me")
    @Operation(summary = "Bio Sayfasını Oluştur veya Güncelle", description = "Kullanıcının bio profilini, temasını, sosyal medya linklerini ve link listesini kaydeder")
    public ResponseEntity<BioPageDto> updateMyBioPage(@RequestBody BioPageUpdateRequest request) {
        BioPageDto saved = bioPageService.createOrUpdateBioPage(request);
        return ResponseEntity.ok(saved);
    }
}
