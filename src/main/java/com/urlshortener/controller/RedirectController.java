package com.urlshortener.controller;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@Controller
@Tag(name = "Yönlendirme Servisi", description = "Kısa linklere tıklayan kullanıcıları Orijinal URL'ye veya Güvenlik Önizleme Sayfasına HTTP 302 ile yönlendiren servis")
public class RedirectController {

    private final UrlShortenerService urlShortenerService;

    @Value("${app.frontend.preview-url:http://localhost:3000/preview/%s}")
    private String previewUrlPattern;

    public RedirectController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]{3,20}}")
    @Operation(summary = "Orijinal URL'ye veya Önizlemeye Yönlendir (Public)", description = "Kısa koda karşılık gelen linki inceler. Önizleme modu aktifse önizleme sayfasına, değilse orijinal hedefe yönlendirir.")
    @ApiResponse(responseCode = "302", description = "Hedef web sitesine veya önizleme sayfasına yönlendiriliyor")
    @ApiResponse(responseCode = "404", description = "Kısa link bulunamadı veya süresi dolmuş")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @PathVariable String shortCode,
            @RequestParam(name = "direct", defaultValue = "false") boolean direct,
            HttpServletRequest request) {

        UrlMapping mapping = urlShortenerService.getUrlMapping(shortCode);

        if (mapping.isPreviewEnabled() && !direct) {
            String previewUrl = String.format(previewUrlPattern, shortCode);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(previewUrl))
                    .build();
        }

        String originalUrl = urlShortenerService.getOriginalUrlAndRecordClick(shortCode, request);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]{3,20}}\\+")
    @Operation(summary = "Önizleme Sayfasına Yönlendir (+ ile)", description = "Kısa kodun sonuna + eklenerek doğrudan güvenlik önizleme sayfasına yönlendirir.")
    public ResponseEntity<Void> redirectToPreviewWithPlus(@PathVariable String shortCode) {
        String previewUrl = String.format(previewUrlPattern, shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(previewUrl))
                .build();
    }

    @GetMapping("/preview/{shortCode:[a-zA-Z0-9_-]{3,20}}")
    @Operation(summary = "Önizleme Sayfasına Yönlendir (/preview/ ile)", description = "Doğrudan /preview/{shortCode} adresinden frontend önizleme sayfasına yönlendirir.")
    public ResponseEntity<Void> redirectToPreviewPath(@PathVariable String shortCode) {
        String previewUrl = String.format(previewUrlPattern, shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(previewUrl))
                .build();
    }
}
