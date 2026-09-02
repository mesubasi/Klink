package com.urlshortener.repository;

import com.urlshortener.model.WorkspacePermissionPolicy;
import com.urlshortener.model.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspacePermissionPolicyRepository extends JpaRepository<WorkspacePermissionPolicy, UUID> {

    Optional<WorkspacePermissionPolicy> findByWorkspaceIdAndRole(UUID workspaceId, WorkspaceRole role);

    List<WorkspacePermissionPolicy> findByWorkspaceId(UUID workspaceId);

    void deleteByWorkspaceId(UUID workspaceId);
}
