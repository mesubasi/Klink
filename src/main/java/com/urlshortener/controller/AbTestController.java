package com.urlshortener.controller;

import com.urlshortener.dto.AbTestConfigResponse;
import com.urlshortener.dto.UpdateAbTestConfigRequest;
import com.urlshortener.service.AbTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls/{shortCode}/ab-test")
@Tag(name = "A/B Split Test Motoru", description = "Tek bir linkin birden fazla hedef sayfaya ağırlıklı dağıtımı ve tıklama performansı kıyaslama servisleri")
public class AbTestController {

    private final AbTestService abTestService;

    public AbTestController(AbTestService abTestService) {
        this.abTestService = abTestService;
    }

    @GetMapping
    @Operation(summary = "A/B Test Yapılandırmasını ve Metriklerini Getir", description = "Kısa linkin varyantlarını, yüzdelik ağırlıklarını ve her bir varyantın aldığı tıklama sayılarını döner.")
    @ApiResponse(responseCode = "200", description = "A/B test verileri başarıyla getirildi")
    public ResponseEntity<AbTestConfigResponse> getAbTestConfig(@PathVariable String shortCode) {
        AbTestConfigResponse response = abTestService.getAbTestConfig(shortCode);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "A/B Testini Yapılandır veya Güncelle", description = "Link için A/B split testini açar, varyantları ve toplamı %100 eden ağırlıkları günceller.")
    @ApiResponse(responseCode = "200", description = "A/B testi başarıyla güncellendi")
    public ResponseEntity<AbTestConfigResponse> updateAbTestConfig(
            @PathVariable String shortCode,
            @Valid @RequestBody UpdateAbTestConfigRequest request) {
        AbTestConfigResponse response = abTestService.updateAbTestConfig(shortCode, request);
        return ResponseEntity.ok(response);
    }
}
