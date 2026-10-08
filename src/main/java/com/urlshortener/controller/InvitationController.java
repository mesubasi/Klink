package com.urlshortener.controller;

import com.urlshortener.dto.AcceptInvitationResponse;
import com.urlshortener.dto.InvitationPreviewResponse;
import com.urlshortener.service.WorkspaceInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invitations")
@Tag(name = "Çalışma Alanı Davetleri", description = "E-posta ile gelen çalışma alanı davetlerini görüntüleme ve kabul etme")
public class InvitationController {

    private final WorkspaceInvitationService invitationService;

    public InvitationController(WorkspaceInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping("/{token}")
    @Operation(summary = "Davet Bilgisini Getir (Public)", description = "Davet bağlantısındaki gizli anahtar için çalışma alanı adı, rol ve davet edilen e-postayı döner.")
    public ResponseEntity<InvitationPreviewResponse> preview(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.preview(token));
    }

    @PostMapping("/{token}/accept")
    @Operation(summary = "Daveti Kabul Et", description = "Giriş yapmış kullanıcının e-postası davetteki e-postayla aynıysa kullanıcıyı çalışma alanına ekler. Davet tek kullanımlıktır.")
    public ResponseEntity<AcceptInvitationResponse> accept(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.accept(token));
    }
}
