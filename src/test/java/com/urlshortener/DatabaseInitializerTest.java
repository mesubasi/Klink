package com.urlshortener;

import com.urlshortener.config.DatabaseInitializer;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DatabaseInitializerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private DatabaseInitializer initializer;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenAnswer(i -> "enc:" + i.getArgument(0));
        initializer = new DatabaseInitializer(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(initializer, "bootstrapUsername", "");
        ReflectionTestUtils.setField(initializer, "bootstrapEmail", "");
        ReflectionTestUtils.setField(initializer, "bootstrapPassword", "");
    }

    @Test
    void demoAccountsAreOnlyCreatedWhenExplicitlyEnabled() {
        ReflectionTestUtils.setField(initializer, "seedDemoUsers", true);
        initializer.run();
        ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository, times(2)).save(saved.capture());
        assertTrue(saved.getAllValues().stream().anyMatch(u -> "admin".equals(u.getUsername()) && "ROLE_ADMIN".equals(u.getRole())));
        assertTrue(saved.getAllValues().stream().allMatch(UserAccount::isEmailVerified));
    }

    @Test
    void productionDefaultCreatesNoWellKnownAccounts() {
        ReflectionTestUtils.setField(initializer, "seedDemoUsers", false);
        initializer.run();
        verify(userRepository, never()).save(any());
    }

    @Test
    void bootstrapCreatesTheFirstAdminOnlyWhenNoneExists() {
        ReflectionTestUtils.setField(initializer, "seedDemoUsers", false);
        ReflectionTestUtils.setField(initializer, "bootstrapUsername", "owner");
        ReflectionTestUtils.setField(initializer, "bootstrapEmail", "Owner@Klink.com");
        ReflectionTestUtils.setField(initializer, "bootstrapPassword", "a-long-secret-passphrase");

        when(userRepository.existsByRole("ROLE_ADMIN")).thenReturn(false);
        initializer.run();
        ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(saved.capture());
        assertEquals("ROLE_ADMIN", saved.getValue().getRole());
        assertEquals("owner@klink.com", saved.getValue().getEmail());
        assertTrue(saved.getValue().isEmailVerified());

        reset(userRepository);
        when(userRepository.existsByRole("ROLE_ADMIN")).thenReturn(true);
        initializer.run();
        verify(userRepository, never()).save(any());
    }

    @Test
    void weakBootstrapPasswordIsRefused() {
        ReflectionTestUtils.setField(initializer, "seedDemoUsers", false);
        ReflectionTestUtils.setField(initializer, "bootstrapUsername", "owner");
        ReflectionTestUtils.setField(initializer, "bootstrapEmail", "owner@klink.com");
        ReflectionTestUtils.setField(initializer, "bootstrapPassword", "short");
        when(userRepository.existsByRole("ROLE_ADMIN")).thenReturn(false);

        initializer.run();

        verify(userRepository, never()).save(any());
    }

    @Test
    void warnsButDoesNotFailWhenDefaultCredentialsStillExist() {
        ReflectionTestUtils.setField(initializer, "seedDemoUsers", false);
        UserAccount legacy = UserAccount.builder().username("admin").password("enc:admin123").role("ROLE_ADMIN").build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(legacy));
        when(passwordEncoder.matches("admin123", "enc:admin123")).thenReturn(true);

        assertDoesNotThrow(() -> initializer.run());
        verify(passwordEncoder).matches("admin123", "enc:admin123");
    }
}
