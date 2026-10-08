package com.urlshortener;

import com.urlshortener.dto.WorkspaceResponse;
import com.urlshortener.model.UserAccount;
import com.urlshortener.model.Workspace;
import com.urlshortener.model.WorkspaceMember;
import com.urlshortener.model.WorkspaceRole;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspaceRepository;
import com.urlshortener.service.WorkspaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Platform operators (ROLE_ADMIN) must be able to support any customer workspace without being a member. */
class SystemAdminWorkspaceAccessTest {

    private WorkspaceRepository workspaceRepository;
    private WorkspaceMemberRepository memberRepository;
    private UserRepository userRepository;
    private WorkspaceService service;

    private UUID workspaceId;
    private Workspace workspace;
    private UserAccount customerManager;

    @BeforeEach
    void setUp() {
        workspaceRepository = mock(WorkspaceRepository.class);
        memberRepository = mock(WorkspaceMemberRepository.class);
        userRepository = mock(UserRepository.class);
        service = new WorkspaceService(workspaceRepository, memberRepository, userRepository, mock(UrlMappingRepository.class));

        workspaceId = UUID.randomUUID();
        customerManager = UserAccount.builder().id(UUID.randomUUID()).username("mudur").email("mudur@a.com").build();
        workspace = Workspace.builder().id(workspaceId).name("A Firması").slug("a-firmasi")
                .owner(customerManager).createdAt(1L).build();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.existsById(workspaceId)).thenReturn(true);
        when(memberRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserAccount loginAs(String username, String role) {
        UserAccount user = UserAccount.builder().id(UUID.randomUUID()).username(username).email(username + "@klink.com").role(role).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(role))));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void systemAdminCanManageAWorkspaceTheyAreNotAMemberOf() {
        loginAs("root", "ROLE_ADMIN");
        assertDoesNotThrow(() -> service.requireWorkspaceAdmin(workspaceId));
    }

    @Test
    void systemAdminSeesWorkspaceDetailsAsAdmin() {
        loginAs("root", "ROLE_ADMIN");
        when(memberRepository.findByWorkspaceIdAndUserUsername(workspaceId, "root")).thenReturn(Optional.empty());

        WorkspaceResponse details = service.getWorkspaceDetails(workspaceId);

        assertEquals(WorkspaceRole.ADMIN, details.getCurrentUserRole());
        assertEquals("A Firması", details.getName());
    }

    @Test
    void systemAdminCanListWorkspaceLinksAndRemoveMembersWithoutMembership() {
        UserAccount root = loginAs("root", "ROLE_ADMIN");
        UUID employeeId = UUID.randomUUID();
        UserAccount employee = UserAccount.builder().id(employeeId).username("ahmet").build();
        when(memberRepository.findByWorkspaceIdAndUserId(workspaceId, employeeId)).thenReturn(Optional.of(
                WorkspaceMember.builder().workspace(workspace).user(employee).role(WorkspaceRole.MEMBER).build()));

        assertDoesNotThrow(() -> service.getWorkspaceUrls(workspaceId));
        assertDoesNotThrow(() -> service.removeMember(workspaceId, employeeId));
        verify(memberRepository).deleteByWorkspaceIdAndUserId(workspaceId, employeeId);
        verify(memberRepository, never()).findByWorkspaceIdAndUserUsername(workspaceId, root.getUsername());
    }

    @Test
    void unknownWorkspaceStillFailsForSystemAdmin() {
        loginAs("root", "ROLE_ADMIN");
        UUID missing = UUID.randomUUID();
        when(workspaceRepository.existsById(missing)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.requireWorkspaceAdmin(missing));
    }

    @Test
    void ordinaryUsersStillNeedMembershipAndAdminRole() {
        UserAccount outsider = loginAs("dis", "ROLE_USER");
        when(memberRepository.findByWorkspaceIdAndUserId(workspaceId, outsider.getId())).thenReturn(Optional.empty());
        when(memberRepository.findByWorkspaceIdAndUserUsername(workspaceId, "dis")).thenReturn(Optional.empty());

        assertThrows(SecurityException.class, () -> service.requireWorkspaceAdmin(workspaceId));
        assertThrows(SecurityException.class, () -> service.getWorkspaceDetails(workspaceId));
        assertThrows(SecurityException.class, () -> service.getWorkspaceUrls(workspaceId));

        UserAccount member = loginAs("uye", "ROLE_USER");
        when(memberRepository.findByWorkspaceIdAndUserId(workspaceId, member.getId())).thenReturn(Optional.of(
                WorkspaceMember.builder().workspace(workspace).user(member).role(WorkspaceRole.MEMBER).build()));
        assertThrows(SecurityException.class, () -> service.requireWorkspaceAdmin(workspaceId));
    }

    @Test
    void customerOverviewIsRestrictedToSystemAdmins() {
        loginAs("mudur", "ROLE_USER");
        assertThrows(SecurityException.class, () -> service.getAllWorkspaces());

        loginAs("root", "ROLE_ADMIN");
        when(workspaceRepository.findAll()).thenReturn(List.of(workspace));
        when(memberRepository.countByWorkspaceId(workspaceId)).thenReturn(4L);

        List<WorkspaceResponse> all = service.getAllWorkspaces();

        assertEquals(1, all.size());
        assertEquals(4L, all.get(0).getMemberCount());
        assertEquals("mudur", all.get(0).getOwnerUsername());
    }
}
