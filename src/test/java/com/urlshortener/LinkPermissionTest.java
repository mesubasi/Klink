package com.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.RolePermissionDto;
import com.urlshortener.dto.WorkspacePermissionMatrixResponse;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.UserAccount;
import com.urlshortener.model.Workspace;
import com.urlshortener.model.WorkspaceMember;
import com.urlshortener.model.WorkspaceRole;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspacePermissionPolicyRepository;
import com.urlshortener.repository.WorkspaceRepository;
import com.urlshortener.service.WorkspacePermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Verifies that workspace link permissions follow the manager-configured permission matrix. */
class LinkPermissionTest {

    private WorkspaceMemberRepository memberRepository;
    private ValueOperations<String, Object> valueOps;
    private WorkspacePermissionService service;

    private UUID workspaceId;
    private Workspace workspace;
    private UserAccount creator;
    private UrlMapping workspaceLink;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        memberRepository = mock(WorkspaceMemberRepository.class);
        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        service = new WorkspacePermissionService(
                mock(WorkspacePermissionPolicyRepository.class), mock(WorkspaceRepository.class),
                memberRepository, mock(UserRepository.class), redis, new ObjectMapper(), mock(com.urlshortener.service.AuditService.class));

        workspaceId = UUID.randomUUID();
        workspace = Workspace.builder().id(workspaceId).name("A Firması").slug("a-firmasi").build();
        creator = UserAccount.builder().id(UUID.randomUUID()).username("ahmet").email("a@a.com").role("ROLE_USER").build();
        workspaceLink = UrlMapping.builder().shortCode("camp").originalUrl("https://a.com").user(creator).workspace(workspace).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String username, String... roles) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, null, java.util.Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList()));
    }

    private void joinAs(String username, WorkspaceRole role) {
        when(memberRepository.findByWorkspaceIdAndUserUsername(workspaceId, username))
                .thenReturn(Optional.of(WorkspaceMember.builder().workspace(workspace).role(role).build()));
    }

    private void matrix(RolePermissionDto member, RolePermissionDto viewer) {
        when(valueOps.get(anyString())).thenReturn(new WorkspacePermissionMatrixResponse(
                workspaceId, "A Firması", RolePermissionDto.defaultAdminPreset(), member, viewer, 1L));
    }

    @Test
    void memberFollowsTheMatrixEvenOnLinksTheyCreated() {
        loginAs("ahmet", "ROLE_USER");
        joinAs("ahmet", WorkspaceRole.MEMBER);

        matrix(new RolePermissionDto(true, false, true, true, false, true), RolePermissionDto.defaultViewerPreset());
        assertFalse(service.hasLinkPermission(workspaceLink, "canDeleteLink"), "manager disabled delete for members");
        assertTrue(service.hasLinkPermission(workspaceLink, "canViewAnalytics"));

        matrix(new RolePermissionDto(true, true, false, true, false, false), RolePermissionDto.defaultViewerPreset());
        assertTrue(service.hasLinkPermission(workspaceLink, "canDeleteLink"));
        assertFalse(service.hasLinkPermission(workspaceLink, "canViewAnalytics"));
        assertFalse(service.hasLinkPermission(workspaceLink, "canExportReports"));
    }

    @Test
    void viewerCanOnlyDoWhatTheMatrixGrantsViewers() {
        loginAs("ayse", "ROLE_USER");
        joinAs("ayse", WorkspaceRole.VIEWER);
        matrix(RolePermissionDto.defaultMemberPreset(), new RolePermissionDto(false, false, false, false, false, true));

        assertTrue(service.hasLinkPermission(workspaceLink, "canViewAnalytics"));
        assertFalse(service.hasLinkPermission(workspaceLink, "canDeleteLink"));
        assertFalse(service.hasLinkPermission(workspaceLink, "canCreateLink"));
        assertFalse(service.hasLinkPermission(workspaceLink, "canExportReports"));
    }

    @Test
    void workspaceAdminAndSystemAdminAlwaysAllowed() {
        loginAs("mudur", "ROLE_USER");
        joinAs("mudur", WorkspaceRole.ADMIN);
        matrix(new RolePermissionDto(false, false, false, false, false, false), new RolePermissionDto(false, false, false, false, false, false));
        assertTrue(service.hasLinkPermission(workspaceLink, "canDeleteLink"));
        assertTrue(service.hasLinkPermission(workspaceLink, "canExportReports"));

        loginAs("root", "ROLE_ADMIN");
        assertTrue(service.hasLinkPermission(workspaceLink, "canDeleteLink"));
    }

    @Test
    void creatorWhoLeftTheWorkspaceLosesAccess() {
        loginAs("ahmet", "ROLE_USER");
        when(memberRepository.findByWorkspaceIdAndUserUsername(workspaceId, "ahmet")).thenReturn(Optional.empty());
        matrix(RolePermissionDto.defaultMemberPreset(), RolePermissionDto.defaultViewerPreset());

        assertFalse(service.hasLinkPermission(workspaceLink, "canViewAnalytics"));
        assertFalse(service.hasLinkPermission(workspaceLink, "canDeleteLink"));
    }

    @Test
    void personalLinksAreOwnerOnly() {
        UrlMapping personal = UrlMapping.builder().shortCode("mine").originalUrl("https://a.com").user(creator).build();

        loginAs("ahmet", "ROLE_USER");
        assertTrue(service.hasLinkPermission(personal, "canDeleteLink"));

        loginAs("baska", "ROLE_USER");
        assertFalse(service.hasLinkPermission(personal, "canDeleteLink"));

        loginAs("root", "ROLE_ADMIN");
        assertTrue(service.hasLinkPermission(personal, "canDeleteLink"));
    }

    @Test
    void anonymousUsersAreDenied() {
        SecurityContextHolder.clearContext();
        assertFalse(service.hasLinkPermission(workspaceLink, "canViewAnalytics"));
        loginAs("anonymousUser", "ROLE_ANONYMOUS");
        assertFalse(service.hasLinkPermission(workspaceLink, "canViewAnalytics"));
    }

    @Test
    void uiPermissionsMirrorTheEnforcedOnes() {
        loginAs("ahmet", "ROLE_USER");
        joinAs("ahmet", WorkspaceRole.MEMBER);
        matrix(new RolePermissionDto(true, false, true, true, false, true), RolePermissionDto.defaultViewerPreset());

        RolePermissionDto shown = service.getLinkPermissions(workspaceLink);

        assertFalse(shown.isCanDeleteLink());
        assertTrue(shown.isCanViewAnalytics());
        assertEquals(service.hasLinkPermission(workspaceLink, "canDeleteLink"), shown.isCanDeleteLink());
        assertEquals(service.hasLinkPermission(workspaceLink, "canExportReports"), shown.isCanExportReports());
    }

    @Test
    void uiPermissionsAreAllFalseForOutsidersAndNullForAnonymous() {
        loginAs("dis", "ROLE_USER");
        when(memberRepository.findByWorkspaceIdAndUserUsername(workspaceId, "dis")).thenReturn(Optional.empty());
        RolePermissionDto none = service.getLinkPermissions(workspaceLink);
        assertFalse(none.isCanViewAnalytics() || none.isCanDeleteLink() || none.isCanCreateLink() || none.isCanExportReports());

        SecurityContextHolder.clearContext();
        assertNull(service.getLinkPermissions(workspaceLink));
    }
}
