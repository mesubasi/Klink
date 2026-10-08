package com.urlshortener.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

/** One immutable line of the audit trail. There are deliberately no setters. */
@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_time", columnList = "occurredAt"),
    @Index(name = "idx_audit_workspace_time", columnList = "workspace_id, occurredAt"),
    @Index(name = "idx_audit_actor_time", columnList = "actorUsername, occurredAt"),
    @Index(name = "idx_audit_action_time", columnList = "action, occurredAt")
})
public class AuditEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private Long occurredAt;

    /** Null for events without a logged-in user (e.g. a failed login). */
    @Column(length = 50, updatable = false)
    private String actorUsername;

    @Column(length = 20, updatable = false)
    private String actorRole;

    @Column(nullable = false, length = 60, updatable = false)
    private String action;

    @Column(nullable = false, length = 10, updatable = false)
    private String outcome;

    @Column(length = 30, updatable = false)
    private String targetType;

    @Column(length = 100, updatable = false)
    private String targetId;

    @Column(name = "workspace_id", updatable = false)
    private UUID workspaceId;

    /** The address the server saw. Behind a reverse proxy this is the proxy. */
    @Column(length = 64, updatable = false)
    private String ip;

    /** The client-supplied X-Forwarded-For header; recorded as-is because it can be forged. */
    @Column(length = 255, updatable = false)
    private String forwardedFor;

    @Column(length = 255, updatable = false)
    private String userAgent;

    @Column(length = 1000, updatable = false)
    private String details;

    protected AuditEvent() {}

    public AuditEvent(long occurredAt, String actorUsername, String actorRole, String action, String outcome,
                      String targetType, String targetId, UUID workspaceId, String ip, String forwardedFor,
                      String userAgent, String details) {
        this.occurredAt = occurredAt;
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;
        this.action = action;
        this.outcome = outcome;
        this.targetType = targetType;
        this.targetId = targetId;
        this.workspaceId = workspaceId;
        this.ip = ip;
        this.forwardedFor = forwardedFor;
        this.userAgent = userAgent;
        this.details = details;
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
