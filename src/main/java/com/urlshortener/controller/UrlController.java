package com.urlshortener.controller;

import com.urlshortener.dto.*;
import com.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "URL Kısaltma ve Analitik İşlemleri", description = "Link kısaltma, toplu kısaltma, analitik, QR kod üretimi ve rapor indirme servisleri")
public class UrlController {

    private final UrlShortenerService urlShortenerService;

    public UrlController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @PostMapping("/api/v1/urls/shorten")
    @Operation(summary = "Tekli Link Kısalt", description = "Uzun web adresini kısa Koda dönüştürür. İsteğe bağlı özel kod, parola ve geçerlilik süresi eklenebilir.")
    @ApiResponse(responseCode = "201", description = "Kısa link başarıyla oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya özel kısa kod zaten kullanımda")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = urlShortenerService.shortenUrl(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/api/v1/urls/bulk-shorten")
    @Operation(summary = "Toplu Link Kısalt (Bulk Shorten)", description = "Tek istekte en fazla 50 adede kadar web adresini toplu olarak kısaltır.")
    @ApiResponse(responseCode = "201", description = "Toplu linkler başarıyla oluşturuldu")
    public ResponseEntity<BulkShortenResponse> bulkShortenUrls(@Valid @RequestBody BulkShortenRequest request) {
        BulkShortenResponse response = urlShortenerService.bulkShortenUrls(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/urls/{shortCode}/preview")
    @Operation(summary = "Kısa Link Güvenlik Önizlemesi", description = "Kısa linkin yönlendirileceği hedef URL'yi, alan adı güvenliğini, SSL durumunu ve güvenlik analiz puanını döner.")
    @ApiResponse(responseCode = "200", description = "Önizleme verileri başarıyla getirildi")
    @ApiResponse(responseCode = "404", description = "Kısa link bulunamadı veya süresi doldu")
    public ResponseEntity<UrlPreviewResponse> getUrlPreview(@PathVariable String shortCode) {
        UrlPreviewResponse response = urlShortenerService.getUrlPreview(shortCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/urls/{shortCode}/proceed")
    @Operation(summary = "Önizleme Sonrası Hedef URL'ye Git", description = "Önizleme sayfasından tıklamayı onaylar, analitik kaydeder ve orijinal hedef web adresini döner.")
    @ApiResponse(responseCode = "200", description = "Tıklama kaydedildi ve orijinal URL dönüldü")
    public ResponseEntity<Map<String, String>> proceedFromPreview(
            @PathVariable String shortCode,
            HttpServletRequest servletRequest) {
        String originalUrl = urlShortenerService.proceedFromPreview(shortCode, servletRequest);
        return ResponseEntity.ok(Collections.singletonMap("originalUrl", originalUrl));
    }

    @PostMapping("/api/v1/urls/{shortCode}/verify-password")
    @Operation(summary = "Şifreli Link Doğrulama", description = "Parola korumalı kısa linkler için şifreyi doğrular ve orijinal URL'i döner.")
    @ApiResponse(responseCode = "200", description = "Şifre doğru, orijinal URL dönüldü")
    @ApiResponse(responseCode = "400", description = "Girilen şifre hatalı")
    public ResponseEntity<String> verifyPassword(
            @PathVariable String shortCode,
            @RequestBody PasswordVerifyRequest request,
            HttpServletRequest servletRequest) {
        String originalUrl = urlShortenerService.verifyPasswordAndGetUrl(shortCode, request.getPassword(), servletRequest);
        return ResponseEntity.ok(originalUrl);
    }

    @GetMapping("/api/v1/urls/analytics/{shortCode}")
    @Operation(summary = "Detaylı Link Analitiği", description = "Kısa linkin toplam tıklama sayısını ve son 50 tıklama kaydını (IP, User-Agent, Tarih) döner.")
    @ApiResponse(responseCode = "200", description = "Analitik verileri getirildi")
    public ResponseEntity<UrlStatsResponse> getAnalytics(@PathVariable String shortCode) {
        UrlStatsResponse response = urlShortenerService.getAnalytics(shortCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/urls/analytics/{shortCode}/summary")
    @Operation(summary = "Özet Analitik Dağılımı", description = "Cihaz (Mobil/Masaüstü), Referrer, Ülke/Şehir ve Tarih bazlı tıklama dağılımlarını döner.")
    @ApiResponse(responseCode = "200", description = "Özet analitik grafikleri getirildi")
    public ResponseEntity<AnalyticsSummaryResponse> getAnalyticsSummary(@PathVariable String shortCode) {
        AnalyticsSummaryResponse response = urlShortenerService.getAnalyticsSummary(shortCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/urls/analytics/{shortCode}/export")
    @Operation(summary = "Analitik Raporunu Dışa Aktar (CSV / PDF Export)", description = "Kısa linkin tıklama analitiğini CSV veya PDF formatında indirir.")
    @ApiResponse(responseCode = "200", description = "Rapor dosyası başarıyla indirildi")
    public ResponseEntity<byte[]> exportAnalyticsReport(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "csv") String format) {

        byte[] fileData = urlShortenerService.exportAnalyticsReport(shortCode, format);

        String filename = "klink-analytics-" + shortCode + "." + ("pdf".equalsIgnoreCase(format) ? "pdf" : "csv");
        String contentType = "pdf".equalsIgnoreCase(format) ? "application/pdf" : "text/csv; charset=UTF-8";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileData);
    }

    @PostMapping("/api/v1/urls/analytics/{shortCode}/send-report")
    @Operation(summary = "Haftalık Analitik Raporunu E-posta ile Gönder", description = "Kısa linkin haftalık performans ve tıklama özetini kullanıcının e-posta adresine iletir.")
    public ResponseEntity<Map<String, Object>> sendAnalyticsEmailReport(
            @PathVariable String shortCode,
            @RequestParam(required = false) String email) {
        Map<String, Object> response = urlShortenerService.sendWeeklyEmailReport(shortCode, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/urls")
    @Operation(summary = "Sistemdeki Tüm Linkleri Listele (Admin)", description = "Sistemdeki tüm kısa linkleri listeler (Admin CRM portalı için).")
    public ResponseEntity<List<ShortenResponse>> getAllUrls() {
        List<ShortenResponse> responses = urlShortenerService.getAllUrls();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/v1/urls/my-urls")
    @Operation(summary = "Kullanıcının Kendi Linklerini Listele", description = "O anki kullanıcının açtığı tüm linkleri kasanızda listeler.")
    public ResponseEntity<List<ShortenResponse>> getMyUrls() {
        List<ShortenResponse> responses = urlShortenerService.getMyUrls();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/api/v1/urls/{shortCode}/status")
    @Operation(summary = "Link Durumunu Değiştir (Aktif/Pasif)", description = "Linkin aktiflik durumunu günceller ve pasife alındığında Redis önbelleğini temizler.")
    public ResponseEntity<ShortenResponse> toggleUrlStatus(@PathVariable String shortCode, @RequestParam boolean active) {
        ShortenResponse response = urlShortenerService.toggleUrlStatus(shortCode, active);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/urls/{shortCode}/qrcode")
    @Operation(summary = "QR Kod Üret", description = "Kısa link için PNG formatında dinamik QR Kod resmi üretir.")
    public ResponseEntity<byte[]> getQrCode(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {
        byte[] qrImage = urlShortenerService.generateQrCodeForUrl(shortCode, width, height);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    @DeleteMapping("/api/v1/urls/{shortCode}")
    @Operation(summary = "Kısa Link Sil", description = "Kısa link kaydını ve Redis önbelleğini tamamen siler.")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        urlShortenerService.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }
}
