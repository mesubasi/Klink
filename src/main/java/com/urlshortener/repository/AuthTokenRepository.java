package com.urlshortener.repository;

import com.urlshortener.model.AuthToken;
import com.urlshortener.model.AuthTokenType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    /** Row-locked lookup so a token cannot be redeemed twice concurrently. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM AuthToken t WHERE t.tokenHash = :tokenHash AND t.type = :type")
    Optional<AuthToken> findForUpdate(@Param("tokenHash") String tokenHash, @Param("type") AuthTokenType type);

    Optional<AuthToken> findFirstByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, AuthTokenType type);

    /** Invalidates every still-open token of a kind for a user when a newer one is issued or the flow completes. */
    @Modifying
    @Query("UPDATE AuthToken t SET t.usedAt = :now WHERE t.user.id = :userId AND t.type = :type AND t.usedAt IS NULL")
    void invalidateOpenTokens(@Param("userId") UUID userId, @Param("type") AuthTokenType type, @Param("now") long now);

    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.expiresAt < :before")
    int deleteExpiredBefore(@Param("before") long before);
}
