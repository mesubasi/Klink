package com.urlshortener.repository;

import com.urlshortener.model.BioLinkItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BioLinkItemRepository extends JpaRepository<BioLinkItem, UUID> {

    List<BioLinkItem> findByBioPageIdOrderBySortOrderAsc(UUID bioPageId);

    @Modifying
    @Query("UPDATE BioLinkItem l SET l.clickCount = l.clickCount + 1 WHERE l.id = :id")
    void incrementClickCount(@Param("id") UUID id);
}
