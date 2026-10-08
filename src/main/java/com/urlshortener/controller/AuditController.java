package com.urlshortener.controller;

import com.urlshortener.dto.AuditEventResponse;
import com.urlshortener.dto.PagedResponse;
import com.urlshortener.model.AuditAction;
import com.urlshortener.service.AuditQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Denetim Kaydı", description = "Kim, ne zaman, neyi yaptı: değiştirilemez denetim izi")
public class AuditController {

    private final AuditQueryService auditQueryService;

    public AuditController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping("/api/v1/admin/audit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Platform Denetim Kaydını Ara (Admin)", description = "Tüm platformdaki denetim olaylarını kullanıcı, işlem, sonuç, çalışma alanı ve tarih aralığına göre filtreler. En yeni olay önce gelir.")
    public ResponseEntity<PagedResponse<AuditEventResponse>> searchAll(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditQueryService.search(actor, action, outcome, workspaceId, from, to, page, size));
    }

    @GetMapping("/api/v1/admin/audit/actions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Denetim İşlem Türleri (Admin)", description = "Filtrelerde kullanılabilecek işlem kodlarını listeler.")
    public ResponseEntity<List<String>> actions() {
        return ResponseEntity.ok(Arrays.stream(AuditAction.values()).map(Enum::name).toList());
    }

    @GetMapping("/api/v1/workspaces/{workspaceId}/audit")
    @Operation(summary = "Çalışma Alanı Denetim Kaydı", description = "Çalışma alanı yöneticisi, kendi çalışma alanında olan olayları görür; platform yöneticisinin buradaki erişimleri de dahildir.")
    public ResponseEntity<PagedResponse<AuditEventResponse>> searchWorkspace(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditQueryService.searchWorkspace(workspaceId, actor, action, from, to, page, size));
    }
}
