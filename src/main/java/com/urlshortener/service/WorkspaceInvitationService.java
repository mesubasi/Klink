package com.urlshortener.service;

import com.urlshortener.dto.AcceptInvitationResponse;
import com.urlshortener.dto.AddWorkspaceMemberRequest;
import com.urlshortener.dto.InvitationPreviewResponse;
import com.urlshortener.dto.InviteMemberResponse;
import com.urlshortener.dto.WorkspaceInvitationResponse;
import com.urlshortener.model.AuditAction;
import com.urlshortener.model.InvitationStatus;
import com.urlshortener.model.UserAccount;
import com.urlshortener.model.Workspace;
import com.urlshortener.model.WorkspaceInvitation;
import com.urlshortener.model.WorkspaceMember;
import com.urlshortener.model.WorkspaceRole;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceInvitationRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspaceRepository;
import com.urlshortener.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Email invitations to a workspace. Registered users are added straight away; everyone else gets a
 * single-use, time-limited link that only works for the invited email address.
 */
@Service
public class WorkspaceInvitationService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceInvitationService.class);
    private static final String INVALID_INVITATION = "Davet bağlantısı geçersiz veya süresi dolmuş.";

    private final WorkspaceInvitationRepository invitationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final EmailService emailService;
    private final EmailVerificationPolicy verificationPolicy;
    private final QuotaService quotaService;
    private final AuditService auditService;

    @Value("${app.invitation.expiry-days:7}")
    private int expiryDays;

    @Value("${app.frontend.invite-url:http://localhost:3000/invite/%s}")
    private String inviteUrlPattern;

    public WorkspaceInvitationService(WorkspaceInvitationRepository invitationRepository,
                                      WorkspaceRepository workspaceRepository,
                                      WorkspaceMemberRepository memberRepository,
                                      UserRepository userRepository,
                                      WorkspaceService workspaceService,
                                      EmailService emailService,
                                      EmailVerificationPolicy verificationPolicy,
                                      QuotaService quotaService,
                                      AuditService auditService) {
        this.auditService = auditService;
        this.quotaService = quotaService;
        this.verificationPolicy = verificationPolicy;
        this.invitationRepository = invitationRepository;
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.workspaceService = workspaceService;
        this.emailService = emailService;
    }

    @Transactional
    public InviteMemberResponse invite(UUID workspaceId, AddWorkspaceMemberRequest request) {
        UserAccount inviter = workspaceService.requireWorkspaceAdmin(workspaceId);
        String email = request.getEmail().trim().toLowerCase();
        WorkspaceRole role = request.getRole() != null ? request.getRole() : WorkspaceRole.MEMBER;

        // Already registered: no invitation needed, add them directly.
        if (userRepository.findByEmail(email).isPresent()) {
            return InviteMemberResponse.added(workspaceService.addMember(workspaceId, request));
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Çalışma alanı bulunamadı."));

        // Re-inviting replaces the earlier link, which stops working.
        List<WorkspaceInvitation> earlier = invitationRepository.findByWorkspaceIdAndEmailAndStatus(workspaceId, email, InvitationStatus.PENDING);
        for (WorkspaceInvitation old : earlier) {
            old.setStatus(InvitationStatus.REVOKED);
            invitationRepository.save(old);
        }
        // Pending invitations occupy a seat, so the replaced ones are not counted twice.
        quotaService.checkMemberQuota(workspace, earlier.isEmpty() ? 1 : 0);

        String token = TokenUtil.generate();
        WorkspaceInvitation invitation = new WorkspaceInvitation();
        invitation.setWorkspace(workspace);
        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setTokenHash(TokenUtil.hash(token));
        invitation.setInvitedBy(inviter);
        invitation.setCreatedAt(System.currentTimeMillis());
        invitation.setExpiresAt(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expiryDays));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation = invitationRepository.save(invitation);

        String inviteUrl = String.format(inviteUrlPattern, token);
        boolean emailSent = emailService.sendWorkspaceInvitation(
                email, workspace.getName(), inviter.getUsername(), role.name(), inviteUrl, invitation.getExpiresAt());

        auditService.record(AuditAction.INVITATION_SENT, "INVITATION", invitation.getId().toString(), workspaceId,
                "email=" + email + " role=" + role + " emailSent=" + emailSent);
        log.info("Çalışma alanı daveti oluşturuldu: workspace={} email={} role={} emailSent={}", workspace.getName(), email, role, emailSent);
        return InviteMemberResponse.invited(toResponse(invitation), emailSent, inviteUrl);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceInvitationResponse> listPending(UUID workspaceId) {
        workspaceService.requireWorkspaceAdmin(workspaceId);
        return invitationRepository.findByWorkspaceIdAndStatusOrderByCreatedAtDesc(workspaceId, InvitationStatus.PENDING).stream()
                .filter(i -> !i.isExpired())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revoke(UUID workspaceId, UUID invitationId) {
        workspaceService.requireWorkspaceAdmin(workspaceId);
        WorkspaceInvitation invitation = invitationRepository.findByIdAndWorkspaceId(invitationId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Davet bulunamadı."));
        if (invitation.getStatus() == InvitationStatus.PENDING) {
            invitation.setStatus(InvitationStatus.REVOKED);
            invitationRepository.save(invitation);
            auditService.record(AuditAction.INVITATION_REVOKED, "INVITATION", invitationId.toString(), workspaceId, "email=" + invitation.getEmail());
        }
    }

    /** Public lookup used by the invite page; invalid, used, revoked and expired links all look the same. */
    @Transactional(readOnly = true)
    public InvitationPreviewResponse preview(String token) {
        WorkspaceInvitation invitation = invitationRepository.findByTokenHash(TokenUtil.hash(token))
                .filter(this::isRedeemable)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_INVITATION));
        return new InvitationPreviewResponse(
                invitation.getWorkspace().getName(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getInvitedBy() != null ? invitation.getInvitedBy().getUsername() : null,
                invitation.getExpiresAt());
    }

    @Transactional
    public AcceptInvitationResponse accept(String token) {
        UserAccount user = getCurrentAuthenticatedUser();
        verificationPolicy.requireVerified(user);

        WorkspaceInvitation invitation = invitationRepository.findByTokenHashForUpdate(TokenUtil.hash(token))
                .filter(this::isRedeemable)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_INVITATION));

        if (user.getEmail() == null || !user.getEmail().trim().equalsIgnoreCase(invitation.getEmail())) {
            auditService.denied("accept invitation for another email", "INVITATION", invitation.getId().toString(), invitation.getWorkspace().getId());
            throw new SecurityException("Bu davet başka bir e-posta adresi için oluşturulmuş. Davet edilen e-posta adresiyle giriş yapın.");
        }

        Workspace workspace = invitation.getWorkspace();
        if (!memberRepository.existsByWorkspaceIdAndUserUsername(workspace.getId(), user.getUsername())) {
            // The invitation already holds a seat, so accepting it never needs extra room.
            memberRepository.save(WorkspaceMember.builder()
                    .workspace(workspace)
                    .user(user)
                    .role(invitation.getRole())
                    .joinedAt(System.currentTimeMillis())
                    .build());
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(System.currentTimeMillis());
        invitationRepository.save(invitation);

        auditService.record(AuditAction.INVITATION_ACCEPTED, "INVITATION", invitation.getId().toString(), workspace.getId(),
                "user=" + user.getUsername() + " role=" + invitation.getRole());
        log.info("Çalışma alanı daveti kabul edildi: user={} workspace={} role={}", user.getUsername(), workspace.getName(), invitation.getRole());
        return new AcceptInvitationResponse(workspace.getId(), workspace.getName(), invitation.getRole());
    }

    private boolean isRedeemable(WorkspaceInvitation invitation) {
        return invitation.getStatus() == InvitationStatus.PENDING && !invitation.isExpired();
    }

    private WorkspaceInvitationResponse toResponse(WorkspaceInvitation i) {
        return new WorkspaceInvitationResponse(i.getId(), i.getEmail(), i.getRole(),
                i.getInvitedBy() != null ? i.getInvitedBy().getUsername() : null, i.getCreatedAt(), i.getExpiresAt());
    }

    private UserAccount getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalArgumentException("Bu işlem için oturum açmanız gerekmektedir.");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı: " + auth.getName()));
    }
}
