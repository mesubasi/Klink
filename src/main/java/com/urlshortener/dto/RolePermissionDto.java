package com.urlshortener.dto;

import java.io.Serializable;

public class RolePermissionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean canCreateLink;
    private boolean canDeleteLink;
    private boolean canExportReports;
    private boolean canCustomizeQr;
    private boolean canManageWebhooks;
    private boolean canViewAnalytics;

    public RolePermissionDto() {}

    public RolePermissionDto(boolean canCreateLink, boolean canDeleteLink, boolean canExportReports,
                             boolean canCustomizeQr, boolean canManageWebhooks, boolean canViewAnalytics) {
        this.canCreateLink = canCreateLink;
        this.canDeleteLink = canDeleteLink;
        this.canExportReports = canExportReports;
        this.canCustomizeQr = canCustomizeQr;
        this.canManageWebhooks = canManageWebhooks;
        this.canViewAnalytics = canViewAnalytics;
    }

    public static RolePermissionDto defaultMemberPreset() {
        return new RolePermissionDto(true, true, true, true, false, true);
    }

    public static RolePermissionDto defaultViewerPreset() {
        return new RolePermissionDto(false, false, true, false, false, true);
    }

    public static RolePermissionDto defaultAdminPreset() {
        return new RolePermissionDto(true, true, true, true, true, true);
    }

    public boolean isCanCreateLink() { return canCreateLink; }
    public void setCanCreateLink(boolean canCreateLink) { this.canCreateLink = canCreateLink; }

    public boolean isCanDeleteLink() { return canDeleteLink; }
    public void setCanDeleteLink(boolean canDeleteLink) { this.canDeleteLink = canDeleteLink; }

    public boolean isCanExportReports() { return canExportReports; }
    public void setCanExportReports(boolean canExportReports) { this.canExportReports = canExportReports; }

    public boolean isCanCustomizeQr() { return canCustomizeQr; }
    public void setCanCustomizeQr(boolean canCustomizeQr) { this.canCustomizeQr = canCustomizeQr; }

    public boolean isCanManageWebhooks() { return canManageWebhooks; }
    public void setCanManageWebhooks(boolean canManageWebhooks) { this.canManageWebhooks = canManageWebhooks; }

    public boolean isCanViewAnalytics() { return canViewAnalytics; }
    public void setCanViewAnalytics(boolean canViewAnalytics) { this.canViewAnalytics = canViewAnalytics; }
}
