package com.urlshortener.controller;

import com.urlshortener.dto.WorkspaceResponse;
import com.urlshortener.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/workspaces")
@Tag(name = "Admin Müşteri Yönetimi", description = "Sistem yöneticisi için tüm çalışma alanlarına (müşterilere) genel bakış")
public class AdminWorkspaceController {

    private final WorkspaceService workspaceService;

    public AdminWorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    @Operation(summary = "Tüm Çalışma Alanlarını Listele (Admin)", description = "Platformdaki tüm çalışma alanlarını sahibi, üye ve link sayısıyla listeler.")
    public ResponseEntity<List<WorkspaceResponse>> getAllWorkspaces() {
        return ResponseEntity.ok(workspaceService.getAllWorkspaces());
    }
}
