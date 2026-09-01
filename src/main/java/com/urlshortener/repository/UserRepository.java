package com.urlshortener.repository;

import com.urlshortener.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
