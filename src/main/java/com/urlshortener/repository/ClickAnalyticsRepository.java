package com.urlshortener.repository;

import com.urlshortener.model.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, UUID> {

    List<ClickAnalytics> findByShortCode(String shortCode);

    List<ClickAnalytics> findTop50ByShortCodeOrderByClickedAtDesc(String shortCode);

    long countByShortCode(String shortCode);
}
