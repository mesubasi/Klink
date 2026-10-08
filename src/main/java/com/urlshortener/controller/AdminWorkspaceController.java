package com.urlshortener.controller;

import com.urlshortener.dto.CreateCustomerRequest;
import com.urlshortener.dto.ProvisionCustomerResponse;
import com.urlshortener.dto.UpdateQuotaRequest;
import com.urlshortener.dto.WorkspaceResponse;
import com.urlshortener.service.CustomerProvisioningService;
import com.urlshortener.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/workspaces")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Müşteri Yönetimi", description = "Sistem yöneticisi için tüm çalışma alanlarına (müşterilere) genel bakış")
public class AdminWorkspaceController {

    private final WorkspaceService workspaceService;
    private final CustomerProvisioningService provisioningService;

    public AdminWorkspaceController(WorkspaceService workspaceService, CustomerProvisioningService provisioningService) {
        this.workspaceService = workspaceService;
        this.provisioningService = provisioningService;
    }

    @GetMapping
    @Operation(summary = "Tüm Çalışma Alanlarını Listele (Admin)", description = "Platformdaki tüm çalışma alanlarını sahibi, üye ve link sayısıyla listeler.")
    public ResponseEntity<List<WorkspaceResponse>> getAllWorkspaces() {
        return ResponseEntity.ok(workspaceService.getAllWorkspaces());
    }

    @PostMapping
    @Operation(summary = "Müşteri Çalışma Alanı Oluştur (Admin)", description = "Yeni bir müşteri çalışma alanı açar ve yöneticisini atar. Yönetici kayıtlıysa doğrudan eklenir, değilse e-posta ile davet edilir.")
    public ResponseEntity<ProvisionCustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(provisioningService.provision(request));
    }

    @PutMapping("/{workspaceId}/quota")
    @Operation(summary = "Müşteri Kotasını Güncelle (Admin)", description = "Çalışma alanının üye ve link sınırını belirler; boş veya 0 sınırsız demektir.")
    public ResponseEntity<WorkspaceResponse> updateQuota(@PathVariable UUID workspaceId, @RequestBody UpdateQuotaRequest request) {
        return ResponseEntity.ok(workspaceService.updateQuota(workspaceId, request.getMaxMembers(), request.getMaxLinks()));
    }
}
