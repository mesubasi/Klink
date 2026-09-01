package com.urlshortener.repository;

import com.urlshortener.model.BioPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BioPageRepository extends JpaRepository<BioPage, UUID> {

    Optional<BioPage> findByUsernameIgnoreCase(String username);

    Optional<BioPage> findByUserId(UUID userId);

    boolean existsByUsernameIgnoreCase(String username);

    @Modifying
    @Query("UPDATE BioPage b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void incrementViewCount(@Param("id") UUID id);
}
