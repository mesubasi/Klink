package com.urlshortener.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class UpdatePermissionMatrixRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "MEMBER rolü için izin yapılandırması gereklidir.")
    private RolePermissionDto member;

    @NotNull(message = "VIEWER rolü için izin yapılandırması gereklidir.")
    private RolePermissionDto viewer;

    public UpdatePermissionMatrixRequest() {}

    public UpdatePermissionMatrixRequest(RolePermissionDto member, RolePermissionDto viewer) {
        this.member = member;
        this.viewer = viewer;
    }

    public RolePermissionDto getMember() { return member; }
    public void setMember(RolePermissionDto member) { this.member = member; }

    public RolePermissionDto getViewer() { return viewer; }
    public void setViewer(RolePermissionDto viewer) { this.viewer = viewer; }
}
