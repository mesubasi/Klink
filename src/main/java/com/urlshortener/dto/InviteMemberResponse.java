package com.urlshortener.dto;

/**
 * Result of inviting someone to a workspace. {@code outcome} is {@code ADDED} when the email already
 * belonged to a registered user (added right away) or {@code INVITED} when a pending invitation was created.
 * {@code inviteUrl} is only filled when the invitation email could not be sent, so the inviter can share it manually.
 */
public class InviteMemberResponse {

    private String outcome;
    private WorkspaceMemberResponse member;
    private WorkspaceInvitationResponse invitation;
    private boolean emailSent;
    private String inviteUrl;

    public InviteMemberResponse() {}

    public static InviteMemberResponse added(WorkspaceMemberResponse member) {
        InviteMemberResponse r = new InviteMemberResponse();
        r.outcome = "ADDED";
        r.member = member;
        return r;
    }

    public static InviteMemberResponse invited(WorkspaceInvitationResponse invitation, boolean emailSent, String inviteUrl) {
        InviteMemberResponse r = new InviteMemberResponse();
        r.outcome = "INVITED";
        r.invitation = invitation;
        r.emailSent = emailSent;
        r.inviteUrl = emailSent ? null : inviteUrl;
        return r;
    }

    public String getOutcome() { return outcome; }
    public WorkspaceMemberResponse getMember() { return member; }
    public WorkspaceInvitationResponse getInvitation() { return invitation; }
    public boolean isEmailSent() { return emailSent; }
    public String getInviteUrl() { return inviteUrl; }
}
