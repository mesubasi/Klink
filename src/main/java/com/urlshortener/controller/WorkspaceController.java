package com.urlshortener.controller;

import com.urlshortener.dto.*;
import com.urlshortener.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@Tag(name = "Çalışma Alanı ve Takım Yönetimi (Workspaces & Teams)", description = "Şirket/ekip çalışma alanları oluşturma, üye davet etme ve RBAC rol yönetimi servisleri")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    @Operation(summary = "Yeni Çalışma Alanı Oluştur", description = "Yeni bir şirket veya takım çalışma alanı oluşturur. Oluşturan kullanıcı otomatik olarak WORKSPACE_ADMIN yetkisine sahip olur.")
    @ApiResponse(responseCode = "201", description = "Çalışma alanı başarıyla oluşturuldu")
    public ResponseEntity<WorkspaceResponse> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.createWorkspace(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Kullanıcının Çalışma Alanlarını Listele", description = "O anki kullanıcının üye olduğu veya yönettiği tüm çalışma alanlarını ve rollerini döner.")
    public ResponseEntity<List<WorkspaceResponse>> getUserWorkspaces() {
        List<WorkspaceResponse> responses = workspaceService.getUserWorkspaces();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{workspaceId}")
    @Operation(summary = "Çalışma Alanı Detayını ve Üyelerini Getir", description = "Çalışma alanının temel bilgilerini ve tüm üyelerini rolleriyle birlikte getirir.")
    public ResponseEntity<WorkspaceResponse> getWorkspaceDetails(@PathVariable UUID workspaceId) {
        WorkspaceResponse response = workspaceService.getWorkspaceDetails(workspaceId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workspaceId}/members")
    @Operation(summary = "Çalışma Alanına Yeni Üye Ekle / Davet Et", description = "Sadece WORKSPACE_ADMIN rolündeki kullanıcılar takımına yeni üye ekleyebilir.")
    @ApiResponse(responseCode = "201", description = "Üye başarıyla eklendi")
    public ResponseEntity<WorkspaceMemberResponse> addMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody AddWorkspaceMemberRequest request) {
        WorkspaceMemberResponse response = workspaceService.addMember(workspaceId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{workspaceId}/members/{userId}")
    @Operation(summary = "Üye Rolünü Güncelle (RBAC)", description = "Çalışma alanı yöneticisi, bir üyenin rolünü (ADMIN, MEMBER, VIEWER) günceller.")
    public ResponseEntity<WorkspaceMemberResponse> updateMemberRole(
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        WorkspaceMemberResponse response = workspaceService.updateMemberRole(workspaceId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    @Operation(summary = "Üyeyi Çalışma Alanından Çıkar", description = "Çalışma alanı yöneticisi bir üyeyi çıkarabilir veya üye kendisi çalışma alanından ayrılabilir.")
    public ResponseEntity<Map<String, Object>> removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId) {
        workspaceService.removeMember(workspaceId, userId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Üye çalışma alanından başarıyla çıkarıldı."));
    }

    @GetMapping("/{workspaceId}/urls")
    @Operation(summary = "Çalışma Alanına Ait Linkleri Listele", description = "Çalışma alanına üye olan kullanıcılar çalışma alanındaki tüm linkleri görüntüler.")
    public ResponseEntity<List<ShortenResponse>> getWorkspaceUrls(@PathVariable UUID workspaceId) {
        List<ShortenResponse> responses = workspaceService.getWorkspaceUrls(workspaceId);
        return ResponseEntity.ok(responses);
    }
}
