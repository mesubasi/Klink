package com.urlshortener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.RolePermissionDto;
import com.urlshortener.dto.UpdatePermissionMatrixRequest;
import com.urlshortener.dto.WorkspacePermissionMatrixResponse;
import com.urlshortener.model.*;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspacePermissionPolicyRepository;
import com.urlshortener.repository.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkspacePermissionService {

    private static final Logger log = LoggerFactory.getLogger(WorkspacePermissionService.class);
    private static final String REDIS_PREFIX = "ws_perms:";

    private final WorkspacePermissionPolicyRepository policyRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public WorkspacePermissionService(WorkspacePermissionPolicyRepository policyRepository,
                                      WorkspaceRepository workspaceRepository,
                                      WorkspaceMemberRepository memberRepository,
                                      UserRepository userRepository,
                                      RedisTemplate<String, Object> redisTemplate,
                                      ObjectMapper objectMapper) {
        this.policyRepository = policyRepository;
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public WorkspacePermissionMatrixResponse getPermissionMatrix(UUID workspaceId) {
        String cacheKey = REDIS_PREFIX + workspaceId.toString();

        // 1. Önce Redis Önbelleğini Kontrol Et (Sub-1ms Cache Hit)
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                if (cached instanceof WorkspacePermissionMatrixResponse) {
                    return (WorkspacePermissionMatrixResponse) cached;
                }
                String jsonStr = cached.toString();
                return objectMapper.readValue(jsonStr, WorkspacePermissionMatrixResponse.class);
            }
        } catch (Exception e) {
            log.warn("Redis yetki önbelleği okunurken hata: {}", e.getMessage());
        }

        // 2. Cache Miss: Veritabanından Oku veya Varsayılanları Üret
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Çalışma alanı bulunamadı."));

        Optional<WorkspacePermissionPolicy> memberPolicyOpt = policyRepository.findByWorkspaceIdAndRole(workspaceId, WorkspaceRole.MEMBER);
        Optional<WorkspacePermissionPolicy> viewerPolicyOpt = policyRepository.findByWorkspaceIdAndRole(workspaceId, WorkspaceRole.VIEWER);

        RolePermissionDto memberDto = memberPolicyOpt.map(this::mapToDto).orElseGet(RolePermissionDto::defaultMemberPreset);
        RolePermissionDto viewerDto = viewerPolicyOpt.map(this::mapToDto).orElseGet(RolePermissionDto::defaultViewerPreset);
        RolePermissionDto adminDto = RolePermissionDto.defaultAdminPreset();

        long latestUpdate = Math.max(
                memberPolicyOpt.map(WorkspacePermissionPolicy::getUpdatedAt).orElse(workspace.getCreatedAt()),
                viewerPolicyOpt.map(WorkspacePermissionPolicy::getUpdatedAt).orElse(workspace.getCreatedAt())
        );

        WorkspacePermissionMatrixResponse response = new WorkspacePermissionMatrixResponse(
                workspaceId,
                workspace.getName(),
                adminDto,
                memberDto,
                viewerDto,
                latestUpdate
        );

        // 3. Redis'e Yaz (24 Saat TTL)
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(response), Duration.ofHours(24));
        } catch (Exception e) {
            log.warn("Redis yetki önbelleğine yazılırken hata: {}", e.getMessage());
        }

        return response;
    }

    @Transactional
    public WorkspacePermissionMatrixResponse updatePermissionMatrix(UUID workspaceId, UpdatePermissionMatrixRequest request) {
        UserAccount currentUser = getCurrentAuthenticatedUser();
        requireAdminRole(workspaceId, currentUser);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Çalışma alanı bulunamadı."));

        // 1. MEMBER Politikasını Güncelle veya Oluştur
        WorkspacePermissionPolicy memberPolicy = policyRepository.findByWorkspaceIdAndRole(workspaceId, WorkspaceRole.MEMBER)
                .orElseGet(() -> WorkspacePermissionPolicy.builder()
                        .workspace(workspace)
                        .role(WorkspaceRole.MEMBER)
                        .build());
        applyDtoToPolicy(memberPolicy, request.getMember());
        policyRepository.save(memberPolicy);

        // 2. VIEWER Politikasını Güncelle veya Oluştur
        WorkspacePermissionPolicy viewerPolicy = policyRepository.findByWorkspaceIdAndRole(workspaceId, WorkspaceRole.VIEWER)
                .orElseGet(() -> WorkspacePermissionPolicy.builder()
                        .workspace(workspace)
                        .role(WorkspaceRole.VIEWER)
                        .build());
        applyDtoToPolicy(viewerPolicy, request.getViewer());
        policyRepository.save(viewerPolicy);

        // 3. Redis Cache Invalidation & Yenileme
        String cacheKey = REDIS_PREFIX + workspaceId.toString();
        try {
            redisTemplate.delete(cacheKey);
            log.info("Redis yetki önbelleği temizlendi (Cache Invalidation): {}", cacheKey);
        } catch (Exception e) {
            log.warn("Redis önbellek silme hatası: {}", e.getMessage());
        }

        return getPermissionMatrix(workspaceId);
    }

    public boolean hasPermission(UUID workspaceId, String username, String permissionName) {
        if (workspaceId == null || username == null) {
            return false;
        }

        // 1. Sistem Super Admin kontrolü
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // 2. Çalışma alanı üyelik ve rol kontrolü
        Optional<WorkspaceMember> memberOpt = memberRepository.findByWorkspaceIdAndUserUsername(workspaceId, username);
        if (memberOpt.isEmpty()) {
            return false;
        }

        WorkspaceRole role = memberOpt.get().getRole();
        if (role == WorkspaceRole.ADMIN) {
            return true; // Çalışma alanı yöneticisi her zaman tam yetkilidir
        }

        // 3. Redis tabanlı hızlı matris kontrolü
        WorkspacePermissionMatrixResponse matrix = getPermissionMatrix(workspaceId);
        RolePermissionDto permissions = (role == WorkspaceRole.MEMBER) ? matrix.getMember() : matrix.getViewer();

        if (permissions == null) {
            return false;
        }

        return switch (permissionName) {
            case "canCreateLink" -> permissions.isCanCreateLink();
            case "canDeleteLink" -> permissions.isCanDeleteLink();
            case "canExportReports" -> permissions.isCanExportReports();
            case "canCustomizeQr" -> permissions.isCanCustomizeQr();
            case "canManageWebhooks" -> permissions.isCanManageWebhooks();
            case "canViewAnalytics" -> permissions.isCanViewAnalytics();
            default -> false;
        };
    }

    /**
     * Decides whether the current user may perform {@code permissionName} on the given link.
     * <ul>
     *   <li>System admins can do everything.</li>
     *   <li>Personal links (no workspace) are limited to their owner.</li>
     *   <li>Workspace links follow the workspace role: workspace admins can do everything, members and
     *       viewers get exactly what the workspace's permission matrix grants their role. Link ownership
     *       does not bypass the matrix, and users who left the workspace lose access to its links.</li>
     * </ul>
     */
    public boolean hasLinkPermission(UrlMapping mapping, String permissionName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return false;
        }

        boolean isSystemAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isSystemAdmin) {
            return true;
        }

        if (mapping.getWorkspace() == null) {
            return mapping.getUser() != null && mapping.getUser().getUsername().equals(auth.getName());
        }

        return hasPermission(mapping.getWorkspace().getId(), auth.getName(), permissionName);
    }

    private void applyDtoToPolicy(WorkspacePermissionPolicy policy, RolePermissionDto dto) {
        policy.setCanCreateLink(dto.isCanCreateLink());
        policy.setCanDeleteLink(dto.isCanDeleteLink());
        policy.setCanExportReports(dto.isCanExportReports());
        policy.setCanCustomizeQr(dto.isCanCustomizeQr());
        policy.setCanManageWebhooks(dto.isCanManageWebhooks());
        policy.setCanViewAnalytics(dto.isCanViewAnalytics());
        policy.setUpdatedAt(System.currentTimeMillis());
    }

    private RolePermissionDto mapToDto(WorkspacePermissionPolicy policy) {
        return new RolePermissionDto(
                policy.isCanCreateLink(),
                policy.isCanDeleteLink(),
                policy.isCanExportReports(),
                policy.isCanCustomizeQr(),
                policy.isCanManageWebhooks(),
                policy.isCanViewAnalytics()
        );
    }

    private void requireAdminRole(UUID workspaceId, UserAccount user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSystemSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        WorkspaceMember member = memberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .orElseThrow(() -> new SecurityException("Bu çalışma alanının üyesi değilsiniz."));

        if (!isSystemSuperAdmin && member.getRole() != WorkspaceRole.ADMIN) {
            throw new SecurityException("İzin matrisini yönetmek için Çalışma Alanı Yöneticisi (WORKSPACE_ADMIN) yetkisi gereklidir.");
        }
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
