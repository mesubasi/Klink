package com.urlshortener.dto;

public class ProvisionCustomerResponse {

    private WorkspaceResponse workspace;
    private InviteMemberResponse manager;

    public ProvisionCustomerResponse() {}

    public ProvisionCustomerResponse(WorkspaceResponse workspace, InviteMemberResponse manager) {
        this.workspace = workspace;
        this.manager = manager;
    }

    public WorkspaceResponse getWorkspace() { return workspace; }
    public InviteMemberResponse getManager() { return manager; }
}
