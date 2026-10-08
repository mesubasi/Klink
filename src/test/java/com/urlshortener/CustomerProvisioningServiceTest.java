package com.urlshortener;

import com.urlshortener.dto.AddWorkspaceMemberRequest;
import com.urlshortener.dto.CreateCustomerRequest;
import com.urlshortener.dto.InviteMemberResponse;
import com.urlshortener.dto.ProvisionCustomerResponse;
import com.urlshortener.dto.WorkspaceResponse;
import com.urlshortener.model.Workspace;
import com.urlshortener.model.WorkspaceRole;
import com.urlshortener.service.CustomerProvisioningService;
import com.urlshortener.service.WorkspaceInvitationService;
import com.urlshortener.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerProvisioningServiceTest {

    @Test
    void opensTheWorkspaceThenAppointsTheManagerAsWorkspaceAdmin() {
        WorkspaceService workspaceService = mock(WorkspaceService.class);
        WorkspaceInvitationService invitationService = mock(WorkspaceInvitationService.class);
        Workspace workspace = Workspace.builder().id(UUID.randomUUID()).name("B Firması").slug("b-firmasi").build();
        WorkspaceResponse view = WorkspaceResponse.builder().id(workspace.getId()).name("B Firması").maxMembers(10).build();
        InviteMemberResponse invited = InviteMemberResponse.invited(null, false, "https://x/invite/t");
        when(workspaceService.createCustomerWorkspace("B Firması", "açıklama", 10, 500)).thenReturn(workspace);
        when(workspaceService.toAdminResponse(workspace)).thenReturn(view);
        when(invitationService.invite(any(), any())).thenReturn(invited);

        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setName("B Firması");
        request.setDescription("açıklama");
        request.setManagerEmail("mudur@b.com");
        request.setMaxMembers(10);
        request.setMaxLinks(500);

        ProvisionCustomerResponse response = new CustomerProvisioningService(workspaceService, invitationService).provision(request);

        ArgumentCaptor<AddWorkspaceMemberRequest> invite = ArgumentCaptor.forClass(AddWorkspaceMemberRequest.class);
        verify(invitationService).invite(org.mockito.ArgumentMatchers.eq(workspace.getId()), invite.capture());
        assertEquals("mudur@b.com", invite.getValue().getEmail());
        assertEquals(WorkspaceRole.ADMIN, invite.getValue().getRole());
        assertSame(view, response.getWorkspace());
        assertSame(invited, response.getManager());
    }
}
