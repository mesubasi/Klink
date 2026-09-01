package com.urlshortener.repository;

import com.urlshortener.model.ApiKey;
import com.urlshortener.model.ApiKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<ApiKey> findByStatusOrderByCreatedAtDesc(ApiKeyStatus status);

    List<ApiKey> findAllByOrderByCreatedAtDesc();

    long countByStatus(ApiKeyStatus status);

    @Modifying
    @Query("UPDATE ApiKey k SET k.totalCalls = k.totalCalls + 1, k.lastUsedAt = :lastUsedAt WHERE k.id = :id")
    void recordUsage(@Param("id") UUID id, @Param("lastUsedAt") Long lastUsedAt);
}
