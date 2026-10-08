package com.urlshortener.config;

import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the accounts needed to get started:
 * <ul>
 *   <li>demo accounts ({@code admin/admin123}, {@code user/password}) only when {@code app.seed.demo-users} is on (development);</li>
 *   <li>a first platform admin from {@code BOOTSTRAP_ADMIN_*} when no admin exists yet (production).</li>
 * </ul>
 * It also shouts when well-known demo credentials are still active outside development.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static final int MIN_BOOTSTRAP_PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.demo-users:false}")
    private boolean seedDemoUsers;

    @Value("${app.bootstrap-admin.username:}")
    private String bootstrapUsername;

    @Value("${app.bootstrap-admin.email:}")
    private String bootstrapEmail;

    @Value("${app.bootstrap-admin.password:}")
    private String bootstrapPassword;

    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (seedDemoUsers) {
            seedDemoUsers();
        } else {
            warnAboutDefaultCredentials();
        }
        bootstrapFirstAdmin();
    }

    private void seedDemoUsers() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(UserAccount.builder()
                    .username("admin")
                    .email("admin@klink.local")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_ADMIN")
                    .emailVerified(true)
                    .build());
            log.info("Varsayılan Admin hesabı oluşturuldu (admin / admin123)");
        }

        if (!userRepository.existsByUsername("user")) {
            userRepository.save(UserAccount.builder()
                    .username("user")
                    .email("user@klink.local")
                    .password(passwordEncoder.encode("password"))
                    .role("ROLE_USER")
                    .emailVerified(true)
                    .build());
            log.info("Varsayılan Test kullanıcısı oluşturuldu (user / password)");
        }
    }

    private void bootstrapFirstAdmin() {
        if (bootstrapUsername.isBlank() || bootstrapEmail.isBlank() || bootstrapPassword.isBlank()) {
            return;
        }
        if (userRepository.existsByRole("ROLE_ADMIN")) {
            return;
        }
        if (bootstrapPassword.length() < MIN_BOOTSTRAP_PASSWORD_LENGTH) {
            log.error("BOOTSTRAP_ADMIN_PASSWORD en az {} karakter olmalı; ilk yönetici oluşturulmadı.", MIN_BOOTSTRAP_PASSWORD_LENGTH);
            return;
        }
        if (userRepository.existsByUsername(bootstrapUsername) || userRepository.existsByEmail(bootstrapEmail.toLowerCase())) {
            log.error("BOOTSTRAP_ADMIN kullanıcı adı veya e-postası zaten kayıtlı; ilk yönetici oluşturulmadı.");
            return;
        }
        userRepository.save(UserAccount.builder()
                .username(bootstrapUsername.trim())
                .email(bootstrapEmail.trim().toLowerCase())
                .password(passwordEncoder.encode(bootstrapPassword))
                .role("ROLE_ADMIN")
                .emailVerified(true)
                .build());
        log.info("İlk platform yöneticisi oluşturuldu: {}", bootstrapUsername);
    }

    /** Databases created by older versions may still contain the well-known demo accounts. */
    private void warnAboutDefaultCredentials() {
        warnIfDefault("admin", "admin123");
        warnIfDefault("user", "password");
    }

    private void warnIfDefault(String username, String knownPassword) {
        userRepository.findByUsername(username)
                .filter(u -> passwordEncoder.matches(knownPassword, u.getPassword()))
                .ifPresent(u -> log.error(
                        "GÜVENLİK UYARISI: '{}' hesabı hâlâ herkesçe bilinen varsayılan parolayı kullanıyor. Parolayı hemen değiştirin veya hesabı silin.",
                        username));
    }
}
