package com.urlshortener;

import com.urlshortener.model.AuthToken;
import com.urlshortener.model.AuthTokenType;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.AuthTokenRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.service.AuthTokenService;
import com.urlshortener.service.EmailService;
import com.urlshortener.util.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthTokenServiceTest {

    private AuthTokenRepository tokenRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private AuthTokenService service;
    private UserAccount user;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(AuthTokenRepository.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);
        service = new AuthTokenService(tokenRepository, userRepository, passwordEncoder, emailService);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "verifyUrlPattern", "https://app.test/verify/%s");
        org.springframework.test.util.ReflectionTestUtils.setField(service, "resetUrlPattern", "https://app.test/reset/%s");

        user = UserAccount.builder().id(UUID.randomUUID()).username("ayse").email("ayse@a.com").password("old").role("ROLE_USER").build();
        when(tokenRepository.save(any(AuthToken.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(UserAccount.class))).thenAnswer(i -> i.getArgument(0));
    }

    private AuthToken tokenFor(AuthTokenType type, String raw, long expiresInMs) {
        AuthToken t = new AuthToken();
        t.setUser(user);
        t.setType(type);
        t.setTokenHash(TokenUtil.hash(raw));
        t.setCreatedAt(System.currentTimeMillis());
        t.setExpiresAt(System.currentTimeMillis() + expiresInMs);
        return t;
    }

    @Test
    void verificationMailCarriesAnUnguessableLinkAndStoresOnlyItsHash() {
        when(tokenRepository.findFirstByUserIdAndTypeOrderByCreatedAtDesc(any(), any())).thenReturn(Optional.empty());

        assertTrue(service.sendEmailVerification(user));

        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmailVerification(eq("ayse@a.com"), eq("ayse"), url.capture());
        String raw = url.getValue().substring("https://app.test/verify/".length());
        assertTrue(raw.length() >= 43);

        ArgumentCaptor<AuthToken> saved = ArgumentCaptor.forClass(AuthToken.class);
        verify(tokenRepository).save(saved.capture());
        assertEquals(TokenUtil.hash(raw), saved.getValue().getTokenHash());
        assertNotEquals(raw, saved.getValue().getTokenHash());
        long day = 24L * 60 * 60 * 1000;
        assertTrue(Math.abs(saved.getValue().getExpiresAt() - (System.currentTimeMillis() + day)) < 5_000);
        verify(tokenRepository).invalidateOpenTokens(eq(user.getId()), eq(AuthTokenType.EMAIL_VERIFICATION), anyLong());
    }

    @Test
    void resendIsThrottledAndSkippedForVerifiedUsers() {
        AuthToken recent = tokenFor(AuthTokenType.EMAIL_VERIFICATION, "x", 1000);
        when(tokenRepository.findFirstByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), AuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(recent));
        assertFalse(service.sendEmailVerification(user), "issued a few milliseconds ago");

        user.setEmailVerified(true);
        assertFalse(service.sendEmailVerification(user));
        verifyNoInteractions(emailService);
    }

    @Test
    void verifyingEmailMarksTheUserAndBurnsTheToken() {
        AuthToken stored = tokenFor(AuthTokenType.EMAIL_VERIFICATION, "good", 60_000);
        when(tokenRepository.findForUpdate(TokenUtil.hash("good"), AuthTokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(stored));

        service.verifyEmail("good");

        assertTrue(user.isEmailVerified());
        assertNotNull(stored.getUsedAt());
        assertThrows(IllegalArgumentException.class, () -> {
            when(tokenRepository.findForUpdate(anyString(), any())).thenReturn(Optional.of(stored));
            service.verifyEmail("good");
        });
    }

    @Test
    void expiredUnknownAndBlankTokensAreRejectedIdentically() {
        AuthToken expired = tokenFor(AuthTokenType.PASSWORD_RESET, "old", -1);
        when(tokenRepository.findForUpdate(TokenUtil.hash("old"), AuthTokenType.PASSWORD_RESET)).thenReturn(Optional.of(expired));
        when(tokenRepository.findForUpdate(TokenUtil.hash("nope"), AuthTokenType.PASSWORD_RESET)).thenReturn(Optional.empty());

        String m1 = assertThrows(IllegalArgumentException.class, () -> service.resetPassword("old", "newpass1")).getMessage();
        String m2 = assertThrows(IllegalArgumentException.class, () -> service.resetPassword("nope", "newpass1")).getMessage();
        String m3 = assertThrows(IllegalArgumentException.class, () -> service.resetPassword(" ", "newpass1")).getMessage();
        assertEquals(m1, m2);
        assertEquals(m2, m3);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetChangesThePasswordVerifiesTheEmailAndInvalidatesOtherLinks() {
        AuthToken stored = tokenFor(AuthTokenType.PASSWORD_RESET, "reset", 60_000);
        when(tokenRepository.findForUpdate(TokenUtil.hash("reset"), AuthTokenType.PASSWORD_RESET)).thenReturn(Optional.of(stored));
        when(passwordEncoder.encode("NewPass123")).thenReturn("ENCODED");

        service.resetPassword("reset", "NewPass123");

        assertEquals("ENCODED", user.getPassword());
        assertTrue(user.isEmailVerified());
        assertEquals(1, user.getTokenVersion(), "tokens issued before the reset must stop working");
        assertNotNull(stored.getUsedAt());
        verify(tokenRepository).invalidateOpenTokens(eq(user.getId()), eq(AuthTokenType.PASSWORD_RESET), anyLong());
    }

    @Test
    void forgotPasswordNeverRevealsWhetherTheEmailExists() {
        when(userRepository.findByEmail("ghost@a.com")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.requestPasswordReset("ghost@a.com"));
        assertDoesNotThrow(() -> service.requestPasswordReset(" "));
        verifyNoInteractions(emailService);

        when(userRepository.findByEmail("ayse@a.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndTypeOrderByCreatedAtDesc(any(), any())).thenReturn(Optional.empty());
        service.requestPasswordReset("  Ayse@A.com ");
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordReset(eq("ayse@a.com"), eq("ayse"), url.capture());
        assertTrue(url.getValue().startsWith("https://app.test/reset/"));

        ArgumentCaptor<AuthToken> saved = ArgumentCaptor.forClass(AuthToken.class);
        verify(tokenRepository).save(saved.capture());
        long hour = 60L * 60 * 1000;
        assertTrue(Math.abs(saved.getValue().getExpiresAt() - (System.currentTimeMillis() + hour)) < 5_000);
    }

    @Test
    void repeatedResetRequestsWithinAMinuteSendOnlyOneMail() {
        when(userRepository.findByEmail("ayse@a.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), AuthTokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(tokenFor(AuthTokenType.PASSWORD_RESET, "r", 60_000)));

        service.requestPasswordReset("ayse@a.com");

        verifyNoInteractions(emailService);
    }
}
