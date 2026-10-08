package com.urlshortener.service;

import com.urlshortener.dto.*;
import com.urlshortener.model.*;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceService.class);

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final UrlMappingRepository urlMappingRepository;
    private final QuotaService quotaService;
    private final EmailVerificationPolicy verificationPolicy;

    @Value("${app.domain:http://localhost:8080}")
    private String domain;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository workspaceMemberRepository,
                            UserRepository userRepository,
                            UrlMappingRepository urlMappingRepository,
                            QuotaService quotaService,
                            EmailVerificationPolicy verificationPolicy) {
        this.quotaService = quotaService;
        this.verificationPolicy = verificationPolicy;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.urlMappingRepository = urlMappingRepository;
    }

    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        UserAccount currentUser = getCurrentAuthenticatedUser();
        verificationPolicy.requireVerified(currentUser);
        quotaService.checkCanCreateWorkspace(currentUser);

        String slug = generateSlug(request.getName().trim());

        Workspace workspace = Workspace.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .slug(slug)
                .owner(currentUser)
                .createdAt(System.currentTimeMillis())
                .build();
        quotaService.applyDefaults(workspace);

        workspace = workspaceRepository.save(workspace);

        // Oluşturan kullanıcı çalışma alanının ADMIN rolündeki ilk üyesi olur
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(currentUser)
                .role(WorkspaceRole.ADMIN)
                .joinedAt(System.currentTimeMillis())
                .build();

        workspaceMemberRepository.save(ownerMember);

        log.info("Yeni çalışma alanı oluşturuldu: {} (Yönetici: {})", workspace.getName(), currentUser.getUsername());

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .slug(workspace.getSlug())
                .ownerUsername(currentUser.getUsername())
                .currentUserRole(WorkspaceRole.ADMIN)
                .memberCount(1)
                .linkCount(0)
                .createdAt(workspace.getCreatedAt())
                .maxMembers(workspace.getMaxMembers())
                .maxLinks(workspace.getMaxLinks())
                .build();
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getUserWorkspaces() {
        UserAccount currentUser = getCurrentAuthenticatedUser();

        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUserUsername(currentUser.getUsername());

        return memberships.stream().map(m -> {
            Workspace w = m.getWorkspace();
            long memberCount = workspaceMemberRepository.countByWorkspaceId(w.getId());
            long linkCount = urlMappingRepository.countByWorkspaceId(w.getId());

            return WorkspaceResponse.builder()
                    .id(w.getId())
                    .name(w.getName())
                    .description(w.getDescription())
                    .slug(w.getSlug())
                    .ownerUsername(w.getOwner().getUsername())
                    .currentUserRole(m.getRole())
                    .memberCount(memberCount)
                    .linkCount(linkCount)
                    .createdAt(w.getCreatedAt())
                    .maxMembers(w.getMaxMembers())
                    .maxLinks(w.getMaxLinks())
                    .build();
        }).collect(Collectors.toList());
    }

    /** Platform-wide customer overview for system admins. */
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getAllWorkspaces() {
        if (!isSystemAdmin()) {
            throw new SecurityException("Bu işlem için sistem yöneticisi yetkisi gereklidir.");
        }
        return workspaceRepository.findAll().stream().map(w -> WorkspaceResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .slug(w.getSlug())
                .ownerUsername(w.getOwner().getUsername())
                .memberCount(workspaceMemberRepository.countByWorkspaceId(w.getId()))
                .linkCount(urlMappingRepository.countByWorkspaceId(w.getId()))
                .createdAt(w.getCreatedAt())
                .maxMembers(w.getMaxMembers())
                .maxLinks(w.getMaxLinks())
                .build())
                .sorted(Comparator.comparing(WorkspaceResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /** Sets the plan limits of a customer workspace (null or 0 = unlimited). Platform admins only. */
    @Transactional
    public WorkspaceResponse updateQuota(UUID workspaceId, Integer maxMembers, Integer maxLinks) {
        if (!isSystemAdmin()) {
            throw new SecurityException("Bu işlem için sistem yöneticisi yetkisi gereklidir.");
        }
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Çalışma alanı bulunamadı."));
        workspace.setMaxMembers(QuotaService.normalize(maxMembers));
        workspace.setMaxLinks(QuotaService.normalize(maxLinks));
        workspaceRepository.save(workspace);
        return toAdminResponse(workspace);
    }

    /** Creates a customer workspace owned by the calling platform admin; the manager is added separately. */
    @Transactional
    public Workspace createCustomerWorkspace(String name, String description, Integer maxMembers, Integer maxLinks) {
        if (!isSystemAdmin()) {
            throw new SecurityException("Bu işlem için sistem yöneticisi yetkisi gereklidir.");
        }
        UserAccount admin = getCurrentAuthenticatedUser();
        Workspace workspace = Workspace.builder()
                .name(name.trim())
                .description(description != null && !description.isBlank() ? description.trim() : null)
                .slug(generateSlug(name.trim()))
                .owner(admin)
                .createdAt(System.currentTimeMillis())
                .build();
        quotaService.applyDefaults(workspace);
        if (maxMembers != null) workspace.setMaxMembers(QuotaService.normalize(maxMembers));
        if (maxLinks != null) workspace.setMaxLinks(QuotaService.normalize(maxLinks));
        log.info("Platform yöneticisi müşteri çalışma alanı oluşturdu: {} ({})", workspace.getName(), admin.getUsername());
        return workspaceRepository.save(workspace);
    }

    public WorkspaceResponse toAdminResponse(Workspace w) {
        return WorkspaceResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .slug(w.getSlug())
                .ownerUsername(w.getOwner().getUsername())
                .memberCount(workspaceMemberRepository.countByWorkspaceId(w.getId()))
                .linkCount(urlMappingRepository.countByWorkspaceId(w.getId()))
                .createdAt(w.getCreatedAt())
                .maxMembers(w.getMaxMembers())
                .maxLinks(w.getMaxLinks())
                .build();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceDetails(UUID workspaceId) {
        UserAccount currentUser = getCurrentAuthenticatedUser();

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Çalışma alanı bulunamadı."));

        WorkspaceRole currentRole = workspaceMemberRepository.findByWorkspaceIdAndUserUsername(workspaceId, currentUser.getUsername())
                .map(WorkspaceMember::getRole)
                .orElseGet(() -> {
                    if (isSystemAdmin()) {
                        return WorkspaceRole.ADMIN;
                    }
                    throw new SecurityException("Bu çalışma alanına erişim yetkiniz bulunmamaktadır.");
                });

        List<WorkspaceMember> allMembers = workspaceMemberRepository.findByWorkspaceId(workspaceId);
        List<WorkspaceMemberResponse> memberResponses = allMembers.stream().map(this::buildMemberResponse).collect(Collectors.toList());

        long linkCount = urlMappingRepository.countByWorkspaceId(workspaceId);

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .slug(workspace.getSlug())
                .ownerUsername(workspace.getOwner().getUsername())
                .currentUserRole(currentRole)
                .memberCount(allMembers.size())
                .linkCount(linkCount)
                .createdAt(workspace.getCreatedAt())
                .members(memberResponses)
                .build();
    }

    @Transactional
    public WorkspaceMemberResponse addMember(UUID workspaceId, AddWorkspaceMemberRequest request) {
        UserAccount currentUser = getCurrentAuthenticatedUser();
        requireAdminRole(workspaceId, currentUser);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Çalışma alanı bulunamadı."));

        quotaService.checkMemberQuota(workspace, 1);

        UserAccount targetUser = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Belirtilen e-posta adresine sahip kayıtlı kullanıcı bulunamadı: " + request.getEmail()));

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserUsername(workspaceId, targetUser.getUsername())) {
            throw new IllegalArgumentException("Bu kullanıcı zaten bu çalışma alanının üyesidir.");
        }

        WorkspaceMember newMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(targetUser)
                .role(request.getRole() != null ? request.getRole() : WorkspaceRole.MEMBER)
                .joinedAt(System.currentTimeMillis())
                .build();

        newMember = workspaceMemberRepository.save(newMember);
        log.info("Çalışma alanına üye eklendi: {} -> {} ({})", targetUser.getUsername(), workspace.getName(), newMember.getRole());

        return buildMemberResponse(newMember);
    }

    @Transactional
    public WorkspaceMemberResponse updateMemberRole(UUID workspaceId, UUID userId, UpdateMemberRoleRequest request) {
        UserAccount currentUser = getCurrentAuthenticatedUser();
        requireAdminRole(workspaceId, currentUser);

        WorkspaceMember targetMembership = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bu çalışma alanında bulunamadı."));

        // Eğer son kalan ADMIN'in rolü değiştirilmeye çalışılıyorsa engelle
        if (targetMembership.getRole() == WorkspaceRole.ADMIN && request.getRole() != WorkspaceRole.ADMIN) {
            long adminCount = workspaceMemberRepository.countByWorkspaceIdAndRole(workspaceId, WorkspaceRole.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Çalışma alanındaki son yöneticinin rolü düşürülemez. Önce başka bir yönetici atamalısınız.");
            }
        }

        targetMembership.setRole(request.getRole());
        targetMembership = workspaceMemberRepository.save(targetMembership);

        log.info("Çalışma alanı üye rolü güncellendi: {} -> {}", targetMembership.getUser().getUsername(), targetMembership.getRole());

        return buildMemberResponse(targetMembership);
    }

    @Transactional
    public void removeMember(UUID workspaceId, UUID userId) {
        UserAccount currentUser = getCurrentAuthenticatedUser();

        boolean isSelfLeaving = currentUser.getId().equals(userId);

        if (!isSystemAdmin()) {
            WorkspaceMember callerMembership = workspaceMemberRepository.findByWorkspaceIdAndUserUsername(workspaceId, currentUser.getUsername())
                    .orElseThrow(() -> new SecurityException("Bu çalışma alanının üyesi değilsiniz."));

            if (!isSelfLeaving && callerMembership.getRole() != WorkspaceRole.ADMIN) {
                throw new SecurityException("Üye çıkarmak için Çalışma Alanı Yöneticisi (WORKSPACE_ADMIN) yetkisi gereklidir.");
            }
        }

        WorkspaceMember targetMembership = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bu çalışma alanında bulunamadı."));

        if (targetMembership.getRole() == WorkspaceRole.ADMIN) {
            long adminCount = workspaceMemberRepository.countByWorkspaceIdAndRole(workspaceId, WorkspaceRole.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Çalışma alanındaki tek yönetici ayrılamaz veya silinemez.");
            }
        }

        workspaceMemberRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
        log.info("Üye çalışma alanından çıkarıldı: userId={} workspaceId={}", userId, workspaceId);
    }

    @Transactional(readOnly = true)
    public List<ShortenResponse> getWorkspaceUrls(UUID workspaceId) {
        UserAccount currentUser = getCurrentAuthenticatedUser();

        // Çalışma alanına üyelik kontrolü (Admin, Member veya Viewer olabilir)
        if (!isSystemAdmin()) {
            workspaceMemberRepository.findByWorkspaceIdAndUserUsername(workspaceId, currentUser.getUsername())
                    .orElseThrow(() -> new SecurityException("Bu çalışma alanının linklerini görüntüleme yetkiniz bulunmamaktadır."));
        }

        List<UrlMapping> mappings = urlMappingRepository.findByWorkspaceId(workspaceId);

        return mappings.stream().map(m -> {
            ShortenResponse.Builder b = ShortenResponse.builder()
                    .shortCode(m.getShortCode())
                    .shortUrl(domain + "/" + m.getShortCode())
                    .originalUrl(m.getOriginalUrl())
                    .createdAt(m.getCreatedAt())
                    .expiresAt(m.getExpiresAt())
                    .clickCount(m.getClickCount())
                    .passwordProtected(m.isPasswordProtected())
                    .blockedCountries(m.getBlockedCountries())
                    .blockedIps(m.getBlockedIps())
                    .previewEnabled(m.isPreviewEnabled())
                    .iosUrl(m.getIosUrl())
                    .androidUrl(m.getAndroidUrl())
                    .desktopUrl(m.getDesktopUrl())
                    .webhookUrl(m.getWebhookUrl())
                    .healthStatus(m.getHealthStatus())
                    .lastHealthCheck(m.getLastHealthCheck())
                    .healthStatusCode(m.getHealthStatusCode())
                    .healthErrorMessage(m.getHealthErrorMessage())
                    .healthResponseTimeMs(m.getHealthResponseTimeMs())
                    .workspaceId(m.getWorkspace() != null ? m.getWorkspace().getId().toString() : null)
                    .workspaceName(m.getWorkspace() != null ? m.getWorkspace().getName() : null);

            return b.build();
        }).collect(Collectors.toList());
    }

    private boolean isSystemAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /** Verifies the current caller may manage the workspace and returns them. */
    @Transactional(readOnly = true)
    public UserAccount requireWorkspaceAdmin(UUID workspaceId) {
        UserAccount currentUser = getCurrentAuthenticatedUser();
        requireAdminRole(workspaceId, currentUser);
        return currentUser;
    }

    /** Workspace admins can manage their workspace; system admins can manage any workspace without being a member. */
    private void requireAdminRole(UUID workspaceId, UserAccount user) {
        if (isSystemAdmin()) {
            if (!workspaceRepository.existsById(workspaceId)) {
                throw new IllegalArgumentException("Çalışma alanı bulunamadı.");
            }
            return;
        }

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .orElseThrow(() -> new SecurityException("Bu çalışma alanının üyesi değilsiniz."));

        if (member.getRole() != WorkspaceRole.ADMIN) {
            throw new SecurityException("Bu işlem için Çalışma Alanı Yöneticisi (WORKSPACE_ADMIN) yetkisi gereklidir.");
        }
    }

    private WorkspaceMemberResponse buildMemberResponse(WorkspaceMember member) {
        return WorkspaceMemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .email(member.getUser().getEmail())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    private String generateSlug(String name) {
        String baseSlug = name.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (baseSlug.isEmpty()) {
            baseSlug = "workspace";
        }
        String slug = baseSlug;
        int counter = 1;
        while (workspaceRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
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
