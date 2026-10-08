package com.urlshortener;

import com.urlshortener.dto.AcceptInvitationResponse;
import com.urlshortener.dto.AddWorkspaceMemberRequest;
import com.urlshortener.dto.InvitationPreviewResponse;
import com.urlshortener.dto.InviteMemberResponse;
import com.urlshortener.dto.WorkspaceMemberResponse;
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
import com.urlshortener.service.EmailService;
import com.urlshortener.service.WorkspaceInvitationService;
import com.urlshortener.service.WorkspaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WorkspaceInvitationServiceTest {

    private WorkspaceInvitationRepository invitationRepository;
    private WorkspaceRepository workspaceRepository;
    private WorkspaceMemberRepository memberRepository;
    private UserRepository userRepository;
    private WorkspaceService workspaceService;
    private EmailService emailService;
    private WorkspaceInvitationService service;

    private UUID workspaceId;
    private Workspace workspace;
    private UserAccount manager;

    @BeforeEach
    void setUp() {
        invitationRepository = mock(WorkspaceInvitationRepository.class);
        workspaceRepository = mock(WorkspaceRepository.class);
        memberRepository = mock(WorkspaceMemberRepository.class);
        userRepository = mock(UserRepository.class);
        workspaceService = mock(WorkspaceService.class);
        emailService = mock(EmailService.class);

        service = new WorkspaceInvitationService(invitationRepository, workspaceRepository, memberRepository,
                userRepository, workspaceService, emailService, mock(com.urlshortener.service.EmailVerificationPolicy.class),
                mock(com.urlshortener.service.QuotaService.class));
        ReflectionTestUtils.setField(service, "expiryDays", 7);
        ReflectionTestUtils.setField(service, "inviteUrlPattern", "https://app.test/invite/%s");

        workspaceId = UUID.randomUUID();
        workspace = Workspace.builder().id(workspaceId).name("A Firması").slug("a-firmasi").build();
        manager = UserAccount.builder().id(UUID.randomUUID()).username("mudur").email("mudur@a.com").role("ROLE_USER").build();

        when(workspaceService.requireWorkspaceAdmin(workspaceId)).thenReturn(manager);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(invitationRepository.save(any(WorkspaceInvitation.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(UserAccount user) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    private WorkspaceInvitation pendingFor(String email, String rawTokenHash) {
        WorkspaceInvitation i = new WorkspaceInvitation();
        i.setWorkspace(workspace);
        i.setEmail(email);
        i.setRole(WorkspaceRole.VIEWER);
        i.setTokenHash(rawTokenHash);
        i.setInvitedBy(manager);
        i.setExpiresAt(System.currentTimeMillis() + 60_000);
        i.setStatus(InvitationStatus.PENDING);
        return i;
    }

    @Test
    void invitingAnUnregisteredEmailCreatesAHashedExpiringInvitationAndSendsMail() {
        when(userRepository.findByEmail("yeni@a.com")).thenReturn(Optional.empty());
        when(invitationRepository.findByWorkspaceIdAndEmailAndStatus(workspaceId, "yeni@a.com", InvitationStatus.PENDING))
                .thenReturn(List.of());
        when(emailService.sendWorkspaceInvitation(anyString(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(true);

        InviteMemberResponse response = service.invite(workspaceId, new AddWorkspaceMemberRequest("  Yeni@A.com ", WorkspaceRole.MEMBER));

        assertEquals("INVITED", response.getOutcome());
        assertTrue(response.isEmailSent());
        assertNull(response.getInviteUrl(), "link is only exposed when the email could not be sent");

        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendWorkspaceInvitation(eq("yeni@a.com"), eq("A Firması"), eq("mudur"), eq("MEMBER"), url.capture(), anyLong());
        String token = url.getValue().substring("https://app.test/invite/".length());

        ArgumentCaptor<WorkspaceInvitation> saved = ArgumentCaptor.forClass(WorkspaceInvitation.class);
        verify(invitationRepository).save(saved.capture());
        WorkspaceInvitation invitation = saved.getValue();
        assertEquals("yeni@a.com", invitation.getEmail());
        assertEquals(64, invitation.getTokenHash().length());
        assertNotEquals(token, invitation.getTokenHash(), "the raw token must never be stored");
        assertTrue(token.length() >= 43, "256-bit token expected");
        long sevenDays = 7L * 24 * 60 * 60 * 1000;
        assertTrue(Math.abs(invitation.getExpiresAt() - (System.currentTimeMillis() + sevenDays)) < 5_000);
    }

    @Test
    void whenMailCannotBeSentTheInviterGetsTheLink() {
        when(userRepository.findByEmail("yeni@a.com")).thenReturn(Optional.empty());
        when(invitationRepository.findByWorkspaceIdAndEmailAndStatus(any(), anyString(), any())).thenReturn(List.of());
        when(emailService.sendWorkspaceInvitation(anyString(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(false);

        InviteMemberResponse response = service.invite(workspaceId, new AddWorkspaceMemberRequest("yeni@a.com", WorkspaceRole.VIEWER));

        assertFalse(response.isEmailSent());
        assertTrue(response.getInviteUrl().startsWith("https://app.test/invite/"));
    }

    @Test
    void reInvitingRevokesTheEarlierLink() {
        WorkspaceInvitation old = pendingFor("yeni@a.com", "oldhash");
        when(userRepository.findByEmail("yeni@a.com")).thenReturn(Optional.empty());
        when(invitationRepository.findByWorkspaceIdAndEmailAndStatus(workspaceId, "yeni@a.com", InvitationStatus.PENDING))
                .thenReturn(List.of(old));

        service.invite(workspaceId, new AddWorkspaceMemberRequest("yeni@a.com", WorkspaceRole.MEMBER));

        assertEquals(InvitationStatus.REVOKED, old.getStatus());
    }

    @Test
    void invitingARegisteredUserAddsThemDirectly() {
        AddWorkspaceMemberRequest request = new AddWorkspaceMemberRequest("ahmet@a.com", WorkspaceRole.MEMBER);
        UserAccount ahmet = UserAccount.builder().id(UUID.randomUUID()).username("ahmet").email("ahmet@a.com").build();
        when(userRepository.findByEmail("ahmet@a.com")).thenReturn(Optional.of(ahmet));
        WorkspaceMemberResponse member = WorkspaceMemberResponse.builder().username("ahmet").role(WorkspaceRole.MEMBER).build();
        when(workspaceService.addMember(workspaceId, request)).thenReturn(member);

        InviteMemberResponse response = service.invite(workspaceId, request);

        assertEquals("ADDED", response.getOutcome());
        assertSame(member, response.getMember());
        verify(invitationRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void acceptAddsTheMatchingUserWithTheInvitedRoleOnce() {
        UserAccount invitee = UserAccount.builder().id(UUID.randomUUID()).username("yeni").email("Yeni@A.com").build();
        loginAs(invitee);
        WorkspaceInvitation invitation = pendingFor("yeni@a.com", "h");
        when(invitationRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(invitation));
        when(memberRepository.existsByWorkspaceIdAndUserUsername(workspaceId, "yeni")).thenReturn(false);

        AcceptInvitationResponse response = service.accept("some-token");

        assertEquals(WorkspaceRole.VIEWER, response.getRole());
        ArgumentCaptor<WorkspaceMember> member = ArgumentCaptor.forClass(WorkspaceMember.class);
        verify(memberRepository).save(member.capture());
        assertEquals(WorkspaceRole.VIEWER, member.getValue().getRole());
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertNotNull(invitation.getAcceptedAt());
    }

    @Test
    void acceptIsRefusedForAnotherEmailAndLeavesTheInvitationUsable() {
        UserAccount other = UserAccount.builder().id(UUID.randomUUID()).username("baska").email("baska@x.com").build();
        loginAs(other);
        WorkspaceInvitation invitation = pendingFor("yeni@a.com", "h");
        when(invitationRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(invitation));

        assertThrows(SecurityException.class, () -> service.accept("some-token"));

        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void usedRevokedExpiredAndUnknownLinksAreAllRejectedTheSameWay() {
        UserAccount invitee = UserAccount.builder().id(UUID.randomUUID()).username("yeni").email("yeni@a.com").build();
        loginAs(invitee);

        WorkspaceInvitation used = pendingFor("yeni@a.com", "h1");
        used.setStatus(InvitationStatus.ACCEPTED);
        WorkspaceInvitation revoked = pendingFor("yeni@a.com", "h2");
        revoked.setStatus(InvitationStatus.REVOKED);
        WorkspaceInvitation expired = pendingFor("yeni@a.com", "h3");
        expired.setExpiresAt(System.currentTimeMillis() - 1);

        String message = null;
        for (WorkspaceInvitation bad : new WorkspaceInvitation[]{used, revoked, expired}) {
            when(invitationRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(bad));
            when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.of(bad));
            IllegalArgumentException accept = assertThrows(IllegalArgumentException.class, () -> service.accept("t"));
            IllegalArgumentException preview = assertThrows(IllegalArgumentException.class, () -> service.preview("t"));
            assertEquals(accept.getMessage(), preview.getMessage());
            if (message != null) {
                assertEquals(message, accept.getMessage());
            }
            message = accept.getMessage();
        }

        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertEquals(message, assertThrows(IllegalArgumentException.class, () -> service.preview("nope")).getMessage());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void previewShowsWorkspaceRoleAndInvitedEmail() {
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.of(pendingFor("yeni@a.com", "h")));

        InvitationPreviewResponse preview = service.preview("tok");

        assertEquals("A Firması", preview.getWorkspaceName());
        assertEquals("yeni@a.com", preview.getEmail());
        assertEquals(WorkspaceRole.VIEWER, preview.getRole());
        assertEquals("mudur", preview.getInvitedBy());
    }
}
