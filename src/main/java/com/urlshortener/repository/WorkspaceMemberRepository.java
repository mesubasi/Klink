package com.urlshortener.repository;

import com.urlshortener.model.WorkspaceMember;
import com.urlshortener.model.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    List<WorkspaceMember> findByWorkspaceId(UUID workspaceId);

    List<WorkspaceMember> findByUserUsername(String username);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserUsername(UUID workspaceId, String username);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    boolean existsByWorkspaceIdAndUserUsername(UUID workspaceId, String username);

    long countByWorkspaceId(UUID workspaceId);

    long countByWorkspaceIdAndRole(UUID workspaceId, WorkspaceRole role);

    void deleteByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
}
