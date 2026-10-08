package com.urlshortener;

import com.urlshortener.dto.LoginRequest;
import com.urlshortener.model.AuditAction;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.service.AuditService;
import com.urlshortener.service.AuthService;
import com.urlshortener.service.AuthTokenService;
import com.urlshortener.service.MessageService;
import com.urlshortener.service.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthAuditTest {

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private AuditService auditService;
    private AuthService service;
    private UserAccount user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authenticationManager = mock(AuthenticationManager.class);
        auditService = mock(AuditService.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        when(tokenProvider.generateToken(any(UserAccount.class))).thenReturn("jwt");
        service = new AuthService(userRepository, null, mock(MessageService.class), authenticationManager,
                tokenProvider, mock(TotpService.class), mock(AuthTokenService.class), auditService);
        user = UserAccount.builder().username("ayse").email("a@a.com").role("ROLE_USER").build();
        when(userRepository.findByUsername("ayse")).thenReturn(Optional.of(user));
        SecurityContextHolder.clearContext();
    }

    @Test
    void aWrongPasswordIsRecordedWithoutTheActorAndWithoutThePassword() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("ayse", "S3cret-guess")));

        verify(auditService).recordAs(eq(null), eq(null), eq(AuditAction.LOGIN_FAILED), eq(AuditService.FAILURE),
                eq("USER"), eq("ayse"), eq(null), eq("bad credentials"));
        verify(auditService, never()).record(any(), any(), any(), any(), org.mockito.ArgumentMatchers.contains("S3cret"));
    }

    @Test
    void aSuccessfulLoginIsRecordedWithTheUser() {
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken("ayse", null));

        service.login(new LoginRequest("ayse", "right"));

        verify(auditService).recordAs("ayse", "ROLE_USER", AuditAction.LOGIN_SUCCESS, AuditService.SUCCESS, "USER", "ayse", null, null);
    }

    @Test
    void passwordCheckedAccountsWithTwoFactorAreNotLoggedInYet() {
        user.setTwoFactorEnabled(true);
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken("ayse", null));

        service.login(new LoginRequest("ayse", "right"));

        verify(auditService, never()).recordAs(any(), any(), eq(AuditAction.LOGIN_SUCCESS), any(), any(), any(), any(), any());
    }
}
