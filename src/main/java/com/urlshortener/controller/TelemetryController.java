package com.urlshortener.controller;

import com.urlshortener.dto.LiveClickDto;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.WorkspaceMember;
import com.urlshortener.model.WorkspaceRole;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.service.LiveClickStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/telemetry")
@Tag(name = "Telemetry & Real-Time Stream", description = "Gerçek zamanlı ziyaret akışı (SSE) ve canlı telemetri endpoint'leri")
public class TelemetryController {

    private static final Logger log = LoggerFactory.getLogger(TelemetryController.class);

    private final LiveClickStreamService liveClickStreamService;
    private final UrlMappingRepository urlMappingRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public TelemetryController(LiveClickStreamService liveClickStreamService,
                               UrlMappingRepository urlMappingRepository,
                               WorkspaceMemberRepository workspaceMemberRepository) {
        this.liveClickStreamService = liveClickStreamService;
        this.urlMappingRepository = urlMappingRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Operation(summary = "Canlı Ziyaret Akışı (SSE Stream)",
               description = "Kullanıcının linklerine gelen tıklamaları gerçek zamanlı (Server-Sent Events) olarak akıtır.")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLiveClicks(@RequestParam(required = false) String shortCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Canlı ziyaret akışına erişmek için giriş yapmalısınız.");
        }

        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Eğer belirli bir link dinlenmek isteniyorsa sahiplik doğrulaması yap
        if (shortCode != null && !shortCode.trim().isEmpty()) {
            verifyShortCodeAccess(shortCode.trim(), username, isAdmin);
        }

        return liveClickStreamService.subscribe(username, isAdmin, (shortCode != null && !shortCode.trim().isEmpty()) ? shortCode.trim() : null);
    }

    @Operation(summary = "Son Canlı Tıklama Geçmişi",
               description = "Bellekte tutulan son tıklama olaylarını liste olarak döner.")
    @GetMapping("/recent")
    public ResponseEntity<List<LiveClickDto>> getRecentClicks(@RequestParam(required = false) String shortCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Canlı telemetri verilerine erişmek için oturum açmalısınız.");
        }

        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (shortCode != null && !shortCode.trim().isEmpty()) {
            verifyShortCodeAccess(shortCode.trim(), username, isAdmin);
        }

        List<LiveClickDto> recents = liveClickStreamService.getRecentClicks(
                username, isAdmin, (shortCode != null && !shortCode.trim().isEmpty()) ? shortCode.trim() : null);
        return ResponseEntity.ok(recents);
    }

    @Operation(summary = "Canlı Akış Durumu",
               description = "Mevcut aktif SSE abone sayısını ve telemetri durumunu döner.")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStreamStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("activeSubscribers", liveClickStreamService.getActiveSubscriberCount());
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }

    private void verifyShortCodeAccess(String shortCode, String username, boolean isAdmin) {
        if (isAdmin) {
            return;
        }

        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Belirtilen kısa kod bulunamadı: " + shortCode));

        // 1. Doğrudan sahip mi?
        if (mapping.getUser() != null && mapping.getUser().getUsername().equalsIgnoreCase(username)) {
            return;
        }

        // 2. Çalışma alanı üyesi mi?
        if (mapping.getWorkspace() != null) {
            Optional<WorkspaceMember> member = workspaceMemberRepository.findByWorkspaceIdAndUserUsername(
                    mapping.getWorkspace().getId(), username);
            if (member.isPresent()) {
                return;
            }
        }

        throw new AccessDeniedException("Bu linkin canlı verilerine erişim yetkiniz bulunmuyor.");
    }
}
