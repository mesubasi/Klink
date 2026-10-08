package com.urlshortener.repository;

import com.urlshortener.dto.LinkStatsResponse;
import com.urlshortener.model.UrlMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, UUID> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<UrlMapping> findByUserUsername(String username);

    @Query("SELECT u FROM UrlMapping u WHERE u.user.username = :username AND " +
           "(LOWER(u.shortCode) LIKE :pattern ESCAPE '\\' OR LOWER(u.originalUrl) LIKE :pattern ESCAPE '\\') AND " +
           "(:filter = 'ALL' " +
           "OR (:filter = 'PROTECTED' AND u.passwordHash IS NOT NULL AND u.passwordHash <> '') " +
           "OR (:filter = 'PREVIEW' AND u.previewEnabled = true) " +
           "OR (:filter = 'BROKEN' AND u.healthStatus = 'BROKEN'))")
    Page<UrlMapping> searchByUsername(@Param("username") String username,
                                      @Param("pattern") String pattern,
                                      @Param("filter") String filter,
                                      Pageable pageable);

    @Query("SELECT new com.urlshortener.dto.LinkStatsResponse(" +
           "COUNT(u), COALESCE(SUM(u.clickCount), 0L), " +
           "COALESCE(SUM(CASE WHEN u.passwordHash IS NOT NULL AND u.passwordHash <> '' THEN 1L ELSE 0L END), 0L), " +
           "COALESCE(SUM(CASE WHEN u.healthStatus = 'BROKEN' THEN 1L ELSE 0L END), 0L), " +
           "COALESCE(SUM(CASE WHEN u.healthStatus = 'HEALTHY' THEN 1L ELSE 0L END), 0L)) " +
           "FROM UrlMapping u WHERE u.user.username = :username")
    LinkStatsResponse getStatsByUsername(@Param("username") String username);

    List<UrlMapping> findByWorkspaceId(UUID workspaceId);

    long countByWorkspaceId(UUID workspaceId);

    List<UrlMapping> findByActiveTrue();

    List<UrlMapping> findByHealthStatus(String healthStatus);

    List<UrlMapping> findByActiveTrueAndExpiresAtBefore(Long now);

    @Modifying
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);
}
