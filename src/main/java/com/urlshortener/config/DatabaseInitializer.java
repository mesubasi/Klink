package com.urlshortener.config;

import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            UserAccount admin = UserAccount.builder()
                    .username("admin")
                    .email("admin@klink.local")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("Varsayılan Admin hesabı oluşturuldu (admin / admin123)");
        }

        if (!userRepository.existsByUsername("user")) {
            UserAccount user = UserAccount.builder()
                    .username("user")
                    .email("user@klink.local")
                    .password(passwordEncoder.encode("password"))
                    .role("ROLE_USER")
                    .build();
            userRepository.save(user);
            log.info("Varsayılan Test kullanıcısı oluşturuldu (user / password)");
        }
    }
}
