package com.urlshortener;

import com.urlshortener.model.AuditAction;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.UserAccount;
import com.urlshortener.model.Workspace;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.service.AuditService;
import com.urlshortener.service.MessageService;
import com.urlshortener.service.UrlShortenerService;
import com.urlshortener.service.WorkspacePermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Which link operations leave a trail: workspace links, and anything a platform admin touches that is not theirs. */
@ExtendWith(MockitoExtension.class)
class LinkAuditTest {

    @Mock private UrlMappingRepository urlMappingRepository;
    @Mock private WorkspaceMemberRepository workspaceMemberRepository;
    @Mock private WorkspacePermissionService workspacePermissionService;
    @Mock private AuditService auditService;
    @Mock private MessageService messageService;
    @Mock private com.urlshortener.repository.ClickAnalyticsRepository clickAnalyticsRepository;

    @InjectMocks
    private UrlShortenerService service;

    private UserAccount owner;
    private UUID workspaceId;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "domain", "http://localhost:8080");
        owner = UserAccount.builder().id(UUID.randomUUID()).username("ahmet").build();
        workspaceId = UUID.randomUUID();
        workspace = Workspace.builder().id(workspaceId).name("Acme").slug("acme").build();
        lenient().when(workspacePermissionService.hasLinkPermission(any(), anyString())).thenReturn(true);
        lenient().when(messageService.getMessage(anyString())).thenReturn("no permission");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String name, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                name, null, List.of(new SimpleGrantedAuthority(role))));
    }

    private UrlMapping link(boolean inWorkspace) {
        UrlMapping m = UrlMapping.builder().shortCode("promo").originalUrl("https://example.com/p").user(owner)
                .workspace(inWorkspace ? workspace : null).build();
        when(urlMappingRepository.findByShortCode("promo")).thenReturn(Optional.of(m));
        return m;
    }

    @Test
    void deletingAWorkspaceLinkIsRecordedWithItsWorkspace() {
        loginAs("ahmet", "ROLE_USER");
        link(true);

        service.deleteUrl("promo");

        verify(auditService).record(eq(AuditAction.LINK_DELETED), eq("LINK"), eq("promo"), eq(workspaceId), contains("example.com/p"));
        verify(urlMappingRepository).delete(any(UrlMapping.class));
    }

    @Test
    void deletingYourOwnPersonalLinkIsNotRecorded() {
        loginAs("ahmet", "ROLE_USER");
        link(false);

        service.deleteUrl("promo");

        verify(auditService, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void aPlatformAdminDeletingSomeoneElsesPersonalLinkIsRecordedAndFlagged() {
        loginAs("root", "ROLE_ADMIN");
        link(false);

        service.deleteUrl("promo");

        verify(auditService).record(eq(AuditAction.LINK_DELETED), eq("LINK"), eq("promo"), eq(null), contains("platform admin"));
    }

    @Test
    void statusChangesOfWorkspaceLinksAreRecorded() {
        loginAs("ahmet", "ROLE_USER");
        link(true);

        service.toggleUrlStatus("promo", false);

        verify(auditService).record(eq(AuditAction.LINK_STATUS_CHANGED), eq("LINK"), eq("promo"), eq(workspaceId), contains("active=false"));
    }

    @Test
    void aPlatformAdminReadingCustomerAnalyticsIsRecordedButAnOwnerIsNot() {
        loginAs("root", "ROLE_ADMIN");
        link(true);
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserUsername(workspaceId, "root")).thenReturn(false);

        service.getAnalytics("promo");

        verify(auditService).record(AuditAction.ADMIN_ACCESSED_LINK, "LINK", "promo", workspaceId, "analytics");

        reset(auditService);
        loginAs("ahmet", "ROLE_USER");
        service.getAnalytics("promo");
        verify(auditService, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void aPlatformAdminWhoIsAMemberOfTheWorkspaceIsNotTreatedAsOutside() {
        loginAs("root", "ROLE_ADMIN");
        link(true);
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserUsername(workspaceId, "root")).thenReturn(true);

        service.getAnalytics("promo");

        verify(auditService, never()).record(eq(AuditAction.ADMIN_ACCESSED_LINK), any(), any(), any(), any());
    }

    @Test
    void refusedOperationsAreRecordedAsDenied() {
        loginAs("ayse", "ROLE_USER");
        link(true);
        when(workspacePermissionService.hasLinkPermission(any(), eq("canDeleteLink"))).thenReturn(false);

        assertThrows(SecurityException.class, () -> service.deleteUrl("promo"));

        verify(auditService).denied("canDeleteLink", "LINK", "promo", workspaceId);
        verify(urlMappingRepository, never()).delete(any(UrlMapping.class));
    }
}
