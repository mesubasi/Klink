package com.urlshortener;

import com.urlshortener.dto.*;
import com.urlshortener.model.*;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspaceRepository;
import com.urlshortener.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    private UserAccount testUser;
    private UserAccount memberUser;
    private Workspace testWorkspace;
    private UUID workspaceId;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        testUser = UserAccount.builder()
                .id(UUID.randomUUID())
                .username("company_admin")
                .email("admin@sirket.com")
                .password("encoded_pass")
                .role("ROLE_USER") // Şirket yöneticisi sistem seviyesinde sadece normal USER'dır, ROLE_ADMIN değildir!
                .build();

        memberUser = UserAccount.builder()
                .id(UUID.randomUUID())
                .username("team_member")
                .email("member@sirket.com")
                .password("encoded_pass")
                .role("ROLE_USER")
                .build();

        testWorkspace = Workspace.builder()
                .id(workspaceId)
                .name("Pazarlama Ekibi")
                .slug("pazarlama-ekibi")
                .owner(testUser)
                .createdAt(System.currentTimeMillis())
                .build();

        // Security Context'e company_admin kullanıcısını ROLE_USER yetkisiyle koyuyoruz
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUser.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testCreateWorkspace_Success() {
        when(userRepository.findByUsername("company_admin")).thenReturn(Optional.of(testUser));
        when(workspaceRepository.existsBySlug(anyString())).thenReturn(false);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(testWorkspace);

        CreateWorkspaceRequest request = new CreateWorkspaceRequest("Pazarlama Ekibi", "Açıklama");
        WorkspaceResponse response = workspaceService.createWorkspace(request);

        assertNotNull(response);
        assertEquals("Pazarlama Ekibi", response.getName());
        assertEquals(WorkspaceRole.ADMIN, response.getCurrentUserRole());
        verify(workspaceMemberRepository, times(1)).save(any(WorkspaceMember.class));
    }

    @Test
    void testAddMember_AsWorkspaceAdmin_Success() {
        when(userRepository.findByUsername("company_admin")).thenReturn(Optional.of(testUser));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(testWorkspace));
        when(userRepository.findByEmail("member@sirket.com")).thenReturn(Optional.of(memberUser));
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserUsername(workspaceId, "team_member")).thenReturn(false);

        // company_admin bu çalışma alanında WORKSPACE_ADMIN
        WorkspaceMember adminMember = WorkspaceMember.builder()
                .id(UUID.randomUUID())
                .workspace(testWorkspace)
                .user(testUser)
                .role(WorkspaceRole.ADMIN)
                .build();
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, testUser.getId()))
                .thenReturn(Optional.of(adminMember));

        WorkspaceMember savedMember = WorkspaceMember.builder()
                .id(UUID.randomUUID())
                .workspace(testWorkspace)
                .user(memberUser)
                .role(WorkspaceRole.MEMBER)
                .joinedAt(System.currentTimeMillis())
                .build();
        when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(savedMember);

        AddWorkspaceMemberRequest request = new AddWorkspaceMemberRequest("member@sirket.com", WorkspaceRole.MEMBER);
        WorkspaceMemberResponse response = workspaceService.addMember(workspaceId, request);

        assertNotNull(response);
        assertEquals("team_member", response.getUsername());
        assertEquals(WorkspaceRole.MEMBER, response.getRole());
    }

    @Test
    void testAddMember_AsWorkspaceMember_ThrowsSecurityException() {
        when(userRepository.findByUsername("company_admin")).thenReturn(Optional.of(testUser));

        // Kullanıcı çalışma alanında sadece MEMBER rolünde, ADMIN değil
        WorkspaceMember regularMember = WorkspaceMember.builder()
                .id(UUID.randomUUID())
                .workspace(testWorkspace)
                .user(testUser)
                .role(WorkspaceRole.MEMBER)
                .build();
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, testUser.getId()))
                .thenReturn(Optional.of(regularMember));

        AddWorkspaceMemberRequest request = new AddWorkspaceMemberRequest("member@sirket.com", WorkspaceRole.MEMBER);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                workspaceService.addMember(workspaceId, request)
        );

        assertTrue(ex.getMessage().contains("WORKSPACE_ADMIN"));
    }

    @Test
    void testPreventDemotingLastAdmin() {
        when(userRepository.findByUsername("company_admin")).thenReturn(Optional.of(testUser));

        WorkspaceMember adminMember = WorkspaceMember.builder()
                .id(UUID.randomUUID())
                .workspace(testWorkspace)
                .user(testUser)
                .role(WorkspaceRole.ADMIN)
                .build();
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, testUser.getId()))
                .thenReturn(Optional.of(adminMember));

        // Sadece 1 adet admin var
        when(workspaceMemberRepository.countByWorkspaceIdAndRole(workspaceId, WorkspaceRole.ADMIN)).thenReturn(1L);

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(WorkspaceRole.MEMBER);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.updateMemberRole(workspaceId, testUser.getId(), request)
        );

        assertTrue(ex.getMessage().contains("son yöneticinin rolü düşürülemez"));
    }
}
