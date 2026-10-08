package com.urlshortener.model;

/** Everything the audit trail records. Stored by name, so values must not be renamed once released. */
public enum AuditAction {
    // Accounts and sessions
    USER_REGISTERED,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT_EVERYWHERE,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_COMPLETED,
    EMAIL_VERIFIED,
    TWO_FACTOR_ENABLED,
    TWO_FACTOR_DISABLED,

    // Platform administration
    ADMIN_USER_ROLE_CHANGED,
    ADMIN_USER_DELETED,
    ADMIN_API_KEY_DECISION,
    CUSTOMER_CREATED,
    QUOTA_UPDATED,

    // Platform admin reading a customer's data without being a member
    ADMIN_VIEWED_WORKSPACE,
    ADMIN_VIEWED_WORKSPACE_LINKS,
    ADMIN_VIEWED_PERMISSIONS,
    ADMIN_ACCESSED_LINK,

    // Workspaces
    WORKSPACE_CREATED,
    MEMBER_ADDED,
    MEMBER_ROLE_CHANGED,
    MEMBER_REMOVED,
    INVITATION_SENT,
    INVITATION_REVOKED,
    INVITATION_ACCEPTED,
    PERMISSIONS_UPDATED,

    // Links in a workspace, or touched by a platform admin
    LINK_DELETED,
    LINK_STATUS_CHANGED,
    LINK_REPORT_EXPORTED,

    // Something was refused
    ACCESS_DENIED
}
