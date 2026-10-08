package com.urlshortener.service;

import com.urlshortener.dto.AddWorkspaceMemberRequest;
import com.urlshortener.dto.CreateCustomerRequest;
import com.urlshortener.dto.InviteMemberResponse;
import com.urlshortener.dto.ProvisionCustomerResponse;
import com.urlshortener.model.Workspace;
import com.urlshortener.model.WorkspaceRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Platform admins open a customer's workspace and appoint its manager, who then invites their own staff. */
@Service
public class CustomerProvisioningService {

    private final WorkspaceService workspaceService;
    private final WorkspaceInvitationService invitationService;

    public CustomerProvisioningService(WorkspaceService workspaceService, WorkspaceInvitationService invitationService) {
        this.workspaceService = workspaceService;
        this.invitationService = invitationService;
    }

    @Transactional
    public ProvisionCustomerResponse provision(CreateCustomerRequest request) {
        Workspace workspace = workspaceService.createCustomerWorkspace(
                request.getName(), request.getDescription(), request.getMaxMembers(), request.getMaxLinks());

        InviteMemberResponse manager = invitationService.invite(workspace.getId(),
                new AddWorkspaceMemberRequest(request.getManagerEmail(), WorkspaceRole.ADMIN));

        return new ProvisionCustomerResponse(workspaceService.toAdminResponse(workspace), manager);
    }
}
