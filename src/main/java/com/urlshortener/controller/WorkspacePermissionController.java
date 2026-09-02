package com.urlshortener.controller;

import com.urlshortener.dto.UpdatePermissionMatrixRequest;
import com.urlshortener.dto.WorkspacePermissionMatrixResponse;
import com.urlshortener.service.WorkspacePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/permissions")
@Tag(name = "İzinler ve Güvenlik Matrisi (RBAC Matrix)", description = "Çalışma alanı içi rollerin (MEMBER, VIEWER) ince taneli izinlerini yönetme ve Redis tabanlı sorgulama servisleri")
public class WorkspacePermissionController {

    private final WorkspacePermissionService permissionService;

    public WorkspacePermissionController(WorkspacePermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @Operation(summary = "Çalışma Alanı İzin Matrisini Getir", description = "Çalışma alanındaki MEMBER ve VIEWER rollerine ait güncel izin durumlarını Redis önbelleğinden veya veritabanından getirir.")
    @ApiResponse(responseCode = "200", description = "İzin matrisi başarıyla getirildi")
    public ResponseEntity<WorkspacePermissionMatrixResponse> getPermissionMatrix(@PathVariable UUID workspaceId) {
        WorkspacePermissionMatrixResponse response = permissionService.getPermissionMatrix(workspaceId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "İzin Matrisini Güncelle", description = "Yalnızca WORKSPACE_ADMIN rolündeki şirket yöneticisi, rollerin izinlerini özelleştirebilir. Güncelleme anında Redis önbelleği temizlenir ve yenilenir.")
    @ApiResponse(responseCode = "200", description = "İzin matrisi güncellendi ve Redis önbelleği yenilendi")
    public ResponseEntity<WorkspacePermissionMatrixResponse> updatePermissionMatrix(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdatePermissionMatrixRequest request) {
        WorkspacePermissionMatrixResponse response = permissionService.updatePermissionMatrix(workspaceId, request);
        return ResponseEntity.ok(response);
    }
}
