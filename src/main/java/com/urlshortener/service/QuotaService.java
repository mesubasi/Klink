package com.urlshortener.service;

import com.urlshortener.exception.QuotaExceededException;
import com.urlshortener.model.InvitationStatus;
import com.urlshortener.model.UserAccount;
import com.urlshortener.model.Workspace;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.WorkspaceInvitationRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Plan limits. A workspace may cap its members and links; accounts may cap how many workspaces they create.
 * Platform admins (ROLE_ADMIN) are never blocked so support work is not held up by a customer's plan.
 * A limit of null/0 means unlimited.
 */
@Service
public class QuotaService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final WorkspaceInvitationRepository invitationRepository;
    private final UrlMappingRepository urlMappingRepository;

    @Value("${app.quota.default-max-members:0}")
    private int defaultMaxMembers;

    @Value("${app.quota.default-max-links:0}")
    private int defaultMaxLinks;

    @Value("${app.quota.max-workspaces-per-user:5}")
    private int maxWorkspacesPerUser;

    @Value("${app.quota.self-service-workspaces:true}")
    private boolean selfServiceWorkspaces;

    public QuotaService(WorkspaceRepository workspaceRepository,
                        WorkspaceMemberRepository memberRepository,
                        WorkspaceInvitationRepository invitationRepository,
                        UrlMappingRepository urlMappingRepository) {
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.invitationRepository = invitationRepository;
        this.urlMappingRepository = urlMappingRepository;
    }

    /** Applies the configured plan defaults to a freshly created self-service workspace. */
    public void applyDefaults(Workspace workspace) {
        workspace.setMaxMembers(normalize(defaultMaxMembers));
        workspace.setMaxLinks(normalize(defaultMaxLinks));
    }

    /** Converts "0 or negative" to null (unlimited). */
    public static Integer normalize(Integer limit) {
        return limit == null || limit <= 0 ? null : limit;
    }

    public void checkCanCreateWorkspace(UserAccount user) {
        if (isPlatformAdmin()) {
            return;
        }
        if (!selfServiceWorkspaces) {
            throw new SecurityException("Çalışma alanları yalnızca platform yöneticisi tarafından oluşturulur. Lütfen destek ekibiyle iletişime geçin.");
        }
        if (maxWorkspacesPerUser > 0 && workspaceRepository.countByOwnerId(user.getId()) >= maxWorkspacesPerUser) {
            throw new QuotaExceededException("En fazla " + maxWorkspacesPerUser + " çalışma alanı oluşturabilirsiniz.");
        }
    }

    /** @param additional how many members/invitations are about to be added */
    public void checkMemberQuota(Workspace workspace, int additional) {
        Integer max = workspace.getMaxMembers();
        if (max == null || isPlatformAdmin()) {
            return;
        }
        long used = memberRepository.countByWorkspaceId(workspace.getId())
                + invitationRepository.countByWorkspaceIdAndStatusAndExpiresAtGreaterThan(
                        workspace.getId(), InvitationStatus.PENDING, System.currentTimeMillis());
        if (used + additional > max) {
            throw new QuotaExceededException("Bu çalışma alanının üye sınırı doldu (" + max + "). Daha fazla kişi eklemek için paketinizi yükseltin.");
        }
    }

    public void checkLinkQuota(Workspace workspace) {
        Integer max = workspace.getMaxLinks();
        if (max == null || isPlatformAdmin()) {
            return;
        }
        if (urlMappingRepository.countByWorkspaceId(workspace.getId()) >= max) {
            throw new QuotaExceededException("Bu çalışma alanının link sınırı doldu (" + max + "). Daha fazla link oluşturmak için paketinizi yükseltin.");
        }
    }

    private boolean isPlatformAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
