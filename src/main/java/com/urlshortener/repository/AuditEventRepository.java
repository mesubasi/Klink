package com.urlshortener.repository;

import com.urlshortener.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Append-only: it can add events, query them and expire old ones, but intentionally exposes no update
 * or generic delete (it does not extend JpaRepository).
 */
public interface AuditEventRepository extends Repository<AuditEvent, UUID>, JpaSpecificationExecutor<AuditEvent> {

    AuditEvent save(AuditEvent event);

    @Modifying
    @Query("DELETE FROM AuditEvent e WHERE e.occurredAt < :before")
    int deleteOlderThan(@Param("before") long before);
}
