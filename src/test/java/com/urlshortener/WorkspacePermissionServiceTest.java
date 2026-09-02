package com.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.RolePermissionDto;
import com.urlshortener.dto.UpdatePermissionMatrixRequest;
import com.urlshortener.dto.WorkspacePermissionMatrixResponse;
import com.urlshortener.model.UserAccount;
import com.urlshortener.model.Workspace;
import com.urlshortener.model.WorkspaceMember;
import com.urlshortener.model.WorkspaceRole;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspacePermissionPolicyRepository;
import com.urlshortener.repository.WorkspaceRepository;
import com.urlshortener.service.WorkspacePermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspacePermissionServiceTest {

    @Mock
    private WorkspacePermissionPolicyRepository policyRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private WorkspacePermissionService permissionService;

    private UUID workspaceId;
    private Workspace testWorkspace;
    private UserAccount testAdmin;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        testAdmin = UserAccount.builder()
                .id(UUID.randomUUID())
                .username("admin_user")
                .email("admin@test.com")
                .role("ROLE_USER")
                .build();

        testWorkspace = Workspace.builder()
                .id(workspaceId)
                .name("Test Workspace")
                .slug("test-workspace")
                .owner(testAdmin)
                .createdAt(System.currentTimeMillis())
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testAdmin.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetPermissionMatrix_ReturnsDefaultsWhenEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null); // Cache miss
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(testWorkspace));
        when(policyRepository.findByWorkspaceIdAndRole(workspaceId, WorkspaceRole.MEMBER)).thenReturn(Optional.empty());
        when(policyRepository.findByWorkspaceIdAndRole(workspaceId, WorkspaceRole.VIEWER)).thenReturn(Optional.empty());

        WorkspacePermissionMatrixResponse response = permissionService.getPermissionMatrix(workspaceId);

        assertNotNull(response);
        assertEquals("Test Workspace", response.getWorkspaceName());
        assertTrue(response.getMember().isCanCreateLink());
        assertFalse(response.getViewer().isCanCreateLink());
        verify(valueOperations, times(1)).set(anyString(), any(), any()); // Saved to Redis
    }

    @Test
    void testUpdatePermissionMatrix_AsAdmin_Success() {
        when(userRepository.findByUsername("admin_user")).thenReturn(Optional.of(testAdmin));

        WorkspaceMember adminMember = WorkspaceMember.builder()
                .id(UUID.randomUUID())
                .workspace(testWorkspace)
                .user(testAdmin)
                .role(WorkspaceRole.ADMIN)
                .build();
        when(memberRepository.findByWorkspaceIdAndUserId(workspaceId, testAdmin.getId())).thenReturn(Optional.of(adminMember));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(testWorkspace));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RolePermissionDto newMemberDto = new RolePermissionDto(false, false, true, false, false, true);
        RolePermissionDto newViewerDto = new RolePermissionDto(false, false, false, false, false, true);
        UpdatePermissionMatrixRequest request = new UpdatePermissionMatrixRequest(newMemberDto, newViewerDto);

        WorkspacePermissionMatrixResponse response = permissionService.updatePermissionMatrix(workspaceId, request);

        assertNotNull(response);
        verify(redisTemplate, times(1)).delete(anyString()); // Cache invalidation
        verify(policyRepository, times(2)).save(any());
    }

    @Test
    void testUpdatePermissionMatrix_AsNonAdmin_ThrowsSecurityException() {
        when(userRepository.findByUsername("admin_user")).thenReturn(Optional.of(testAdmin));

        WorkspaceMember regularMember = WorkspaceMember.builder()
                .id(UUID.randomUUID())
                .workspace(testWorkspace)
                .user(testAdmin)
                .role(WorkspaceRole.MEMBER) // Sadece üye
                .build();
        when(memberRepository.findByWorkspaceIdAndUserId(workspaceId, testAdmin.getId())).thenReturn(Optional.of(regularMember));

        RolePermissionDto newMemberDto = new RolePermissionDto(true, true, true, true, false, true);
        RolePermissionDto newViewerDto = new RolePermissionDto(false, false, false, false, false, true);
        UpdatePermissionMatrixRequest request = new UpdatePermissionMatrixRequest(newMemberDto, newViewerDto);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                permissionService.updatePermissionMatrix(workspaceId, request)
        );

        assertTrue(ex.getMessage().contains("WORKSPACE_ADMIN"));
    }
}
