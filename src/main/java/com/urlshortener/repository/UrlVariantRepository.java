package com.urlshortener.repository;

import com.urlshortener.model.UrlVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UrlVariantRepository extends JpaRepository<UrlVariant, UUID> {

    List<UrlVariant> findByUrlMappingId(UUID urlMappingId);

    List<UrlVariant> findByUrlMappingShortCode(String shortCode);

    void deleteByUrlMappingId(UUID urlMappingId);
}
