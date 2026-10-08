package com.urlshortener.dto;

import com.urlshortener.model.AuditEvent;

import java.util.UUID;

public class AuditEventResponse {

    private UUID id;
    private Long occurredAt;
    private String actorUsername;
    private String actorRole;
    private String action;
    private String outcome;
    private String targetType;
    private String targetId;
    private UUID workspaceId;
    private String ip;
    private String forwardedFor;
    private String userAgent;
    private String details;

    public AuditEventResponse() {}

    public static AuditEventResponse from(AuditEvent e) {
        AuditEventResponse r = new AuditEventResponse();
        r.id = e.getId();
        r.occurredAt = e.getOccurredAt();
        r.actorUsername = e.getActorUsername();
        r.actorRole = e.getActorRole();
        r.action = e.getAction();
        r.outcome = e.getOutcome();
        r.targetType = e.getTargetType();
        r.targetId = e.getTargetId();
        r.workspaceId = e.getWorkspaceId();
        r.ip = e.getIp();
        r.forwardedFor = e.getForwardedFor();
        r.userAgent = e.getUserAgent();
        r.details = e.getDetails();
        return r;
    }

    public UUID getId() { return id; }
    public Long getOccurredAt() { return occurredAt; }
    public String getActorUsername() { return actorUsername; }
    public String getActorRole() { return actorRole; }
    public String getAction() { return action; }
    public String getOutcome() { return outcome; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public UUID getWorkspaceId() { return workspaceId; }
    public String getIp() { return ip; }
    public String getForwardedFor() { return forwardedFor; }
    public String getUserAgent() { return userAgent; }
    public String getDetails() { return details; }
}
