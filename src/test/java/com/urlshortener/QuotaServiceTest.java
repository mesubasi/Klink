package com.urlshortener;

import com.urlshortener.exception.QuotaExceededException;
import com.urlshortener.model.InvitationStatus;
import com.urlshortener.model.UserAccount;
import com.urlshortener.model.Workspace;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.WorkspaceInvitationRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspaceRepository;
import com.urlshortener.service.QuotaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuotaServiceTest {

    private WorkspaceRepository workspaceRepository;
    private WorkspaceMemberRepository memberRepository;
    private WorkspaceInvitationRepository invitationRepository;
    private UrlMappingRepository urlMappingRepository;
    private QuotaService service;
    private Workspace workspace;
    private UserAccount user;

    @BeforeEach
    void setUp() {
        workspaceRepository = mock(WorkspaceRepository.class);
        memberRepository = mock(WorkspaceMemberRepository.class);
        invitationRepository = mock(WorkspaceInvitationRepository.class);
        urlMappingRepository = mock(UrlMappingRepository.class);
        service = new QuotaService(workspaceRepository, memberRepository, invitationRepository, urlMappingRepository);
        ReflectionTestUtils.setField(service, "maxWorkspacesPerUser", 2);
        ReflectionTestUtils.setField(service, "selfServiceWorkspaces", true);

        workspace = Workspace.builder().id(UUID.randomUUID()).name("A").slug("a").build();
        user = UserAccount.builder().id(UUID.randomUUID()).username("mudur").build();
        loginAs("mudur", "ROLE_USER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String name, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(name, null, List.of(new SimpleGrantedAuthority(role))));
    }

    private void usage(long members, long pendingInvites) {
        when(memberRepository.countByWorkspaceId(workspace.getId())).thenReturn(members);
        when(invitationRepository.countByWorkspaceIdAndStatusAndExpiresAtGreaterThan(
                org.mockito.ArgumentMatchers.eq(workspace.getId()), org.mockito.ArgumentMatchers.eq(InvitationStatus.PENDING), anyLong()))
                .thenReturn(pendingInvites);
    }

    @Test
    void unlimitedWhenNoLimitIsSet() {
        usage(1000, 1000);
        when(urlMappingRepository.countByWorkspaceId(workspace.getId())).thenReturn(1_000_000L);
        assertDoesNotThrow(() -> service.checkMemberQuota(workspace, 1));
        assertDoesNotThrow(() -> service.checkLinkQuota(workspace));
    }

    @Test
    void pendingInvitationsOccupySeats() {
        workspace.setMaxMembers(5);
        usage(3, 1);
        assertDoesNotThrow(() -> service.checkMemberQuota(workspace, 1));
        usage(3, 2);
        QuotaExceededException ex = assertThrows(QuotaExceededException.class, () -> service.checkMemberQuota(workspace, 1));
        assertTrue(ex.getMessage().contains("5"));
    }

    @Test
    void linkLimitStopsNewLinksOnlyOnceFull() {
        workspace.setMaxLinks(10);
        when(urlMappingRepository.countByWorkspaceId(workspace.getId())).thenReturn(9L);
        assertDoesNotThrow(() -> service.checkLinkQuota(workspace));
        when(urlMappingRepository.countByWorkspaceId(workspace.getId())).thenReturn(10L);
        assertThrows(QuotaExceededException.class, () -> service.checkLinkQuota(workspace));
    }

    @Test
    void platformAdminsAreNeverBlockedByACustomerPlan() {
        workspace.setMaxMembers(1);
        workspace.setMaxLinks(1);
        usage(5, 5);
        when(urlMappingRepository.countByWorkspaceId(workspace.getId())).thenReturn(5L);
        loginAs("root", "ROLE_ADMIN");

        assertDoesNotThrow(() -> service.checkMemberQuota(workspace, 1));
        assertDoesNotThrow(() -> service.checkLinkQuota(workspace));
        when(workspaceRepository.countByOwnerId(user.getId())).thenReturn(99L);
        assertDoesNotThrow(() -> service.checkCanCreateWorkspace(user));
    }

    @Test
    void workspacesPerUserAreCapped() {
        when(workspaceRepository.countByOwnerId(user.getId())).thenReturn(1L);
        assertDoesNotThrow(() -> service.checkCanCreateWorkspace(user));
        when(workspaceRepository.countByOwnerId(user.getId())).thenReturn(2L);
        assertThrows(QuotaExceededException.class, () -> service.checkCanCreateWorkspace(user));
    }

    @Test
    void selfServiceCanBeSwitchedOff() {
        ReflectionTestUtils.setField(service, "selfServiceWorkspaces", false);
        when(workspaceRepository.countByOwnerId(any())).thenReturn(0L);
        assertThrows(SecurityException.class, () -> service.checkCanCreateWorkspace(user));
    }

    @Test
    void defaultsAndNormalization() {
        ReflectionTestUtils.setField(service, "defaultMaxMembers", 10);
        ReflectionTestUtils.setField(service, "defaultMaxLinks", 0);
        service.applyDefaults(workspace);
        assertEquals(10, workspace.getMaxMembers());
        assertNull(workspace.getMaxLinks());

        assertNull(QuotaService.normalize(0));
        assertNull(QuotaService.normalize(-3));
        assertNull(QuotaService.normalize(null));
        assertEquals(7, QuotaService.normalize(7));
    }
}
