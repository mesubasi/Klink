package com.urlshortener;

import com.urlshortener.model.AuditAction;
import com.urlshortener.model.AuditEvent;
import com.urlshortener.repository.AuditEventRepository;
import com.urlshortener.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AuditServiceTest {

    private AuditEventRepository repository;
    private PlatformTransactionManager txManager;
    private AuditService service;

    @BeforeEach
    void setUp() {
        repository = mock(AuditEventRepository.class);
        txManager = mock(PlatformTransactionManager.class);
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        service = new AuditService(repository, txManager);
        ReflectionTestUtils.setField(service, "retentionDays", 30);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private void loginAs(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(role))));
    }

    private void request(String remote, String forwarded, String agent) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr(remote);
        if (forwarded != null) req.addHeader("X-Forwarded-For", forwarded);
        if (agent != null) req.addHeader("User-Agent", agent);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
    }

    private AuditEvent saved() {
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void recordsWhoWhatWhereAndFromWhichAddress() {
        loginAs("root", "ROLE_ADMIN");
        request("10.0.0.7", "203.0.113.9, 10.0.0.1", "JUnit/5");
        UUID workspaceId = UUID.randomUUID();

        service.record(AuditAction.MEMBER_ADDED, "USER", "ahmet", workspaceId, "role=MEMBER");

        AuditEvent e = saved();
        assertEquals("root", e.getActorUsername());
        assertEquals("ROLE_ADMIN", e.getActorRole());
        assertEquals("MEMBER_ADDED", e.getAction());
        assertEquals("SUCCESS", e.getOutcome());
        assertEquals("USER", e.getTargetType());
        assertEquals("ahmet", e.getTargetId());
        assertEquals(workspaceId, e.getWorkspaceId());
        assertEquals("10.0.0.7", e.getIp());
        assertEquals("203.0.113.9, 10.0.0.1", e.getForwardedFor(), "client-supplied header kept apart from the real address");
        assertEquals("JUnit/5", e.getUserAgent());
        assertEquals("role=MEMBER", e.getDetails());
        assertTrue(Math.abs(e.getOccurredAt() - System.currentTimeMillis()) < 5_000);
    }

    @Test
    void eventsAreWrittenInTheirOwnTransactionSoRefusalsSurviveARollback() {
        loginAs("ayse", "ROLE_USER");
        service.denied(AuditAction.LINK_DELETED, "LINK", "abc", null);

        ArgumentCaptor<TransactionDefinition> def = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(txManager).getTransaction(def.capture());
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW, def.getValue().getPropagationBehavior());

        AuditEvent e = saved();
        assertEquals("ACCESS_DENIED", e.getAction());
        assertEquals("DENIED", e.getOutcome());
        assertEquals("attempted=LINK_DELETED", e.getDetails());
    }

    @Test
    void eventsWithoutALoggedInUserHaveNoActor() {
        service.recordAs(null, null, AuditAction.LOGIN_FAILED, AuditService.FAILURE, "USER", "someone", null, "bad credentials");

        AuditEvent e = saved();
        assertNull(e.getActorUsername());
        assertEquals("FAILURE", e.getOutcome());
        assertNull(e.getIp(), "no request in this context");
    }

    @Test
    void untrustedTextCannotForgeLogLinesOrOverflowColumns() {
        loginAs("root", "ROLE_ADMIN");
        request("1.1.1.1", null, "agent\r\nINJECTED");
        String huge = "x".repeat(5000);

        service.record(AuditAction.ADMIN_USER_DELETED, "USER", "bob\nSUCCESS fake", null, huge);

        AuditEvent e = saved();
        assertFalse(e.getTargetId().contains("\n"));
        assertFalse(e.getUserAgent().contains("\r") || e.getUserAgent().contains("\n"));
        assertEquals(1000, e.getDetails().length());
    }

    @Test
    void aFailureToWriteNeverBreaksTheOperationThatTriggeredIt() {
        loginAs("root", "ROLE_ADMIN");
        when(repository.save(any())).thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() -> service.record(AuditAction.QUOTA_UPDATED, "WORKSPACE", "w", null, null));
    }

    @Test
    void retentionDeletesOnlyOldEventsAndCanBeSwitchedOff() {
        when(repository.deleteOlderThan(anyLong())).thenReturn(3);
        service.purgeExpired();

        ArgumentCaptor<Long> cutoff = ArgumentCaptor.forClass(Long.class);
        verify(repository).deleteOlderThan(cutoff.capture());
        long expected = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        assertTrue(Math.abs(cutoff.getValue() - expected) < 5_000);

        reset(repository);
        ReflectionTestUtils.setField(service, "retentionDays", 0);
        service.purgeExpired();
        verifyNoInteractions(repository);
    }

    @Test
    void theRepositoryOffersNoWayToEditOrRemoveIndividualEvents() {
        assertFalse(org.springframework.data.repository.CrudRepository.class.isAssignableFrom(AuditEventRepository.class));
        List<String> methods = java.util.Arrays.stream(AuditEventRepository.class.getDeclaredMethods()).map(m -> m.getName()).sorted().toList();
        assertEquals(List.of("deleteOlderThan", "save"), methods);
    }
}
