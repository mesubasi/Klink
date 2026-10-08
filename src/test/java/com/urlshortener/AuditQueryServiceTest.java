package com.urlshortener;

import com.urlshortener.dto.AuditEventResponse;
import com.urlshortener.dto.PagedResponse;
import com.urlshortener.model.AuditEvent;
import com.urlshortener.repository.AuditEventRepository;
import com.urlshortener.service.AuditQueryService;
import com.urlshortener.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
class AuditQueryServiceTest {

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private AuditEventRepository repository;
    private WorkspaceService workspaceService;
    private AuditQueryService service;
    private final UUID ws1 = UUID.randomUUID();
    private final UUID ws2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = new org.springframework.data.jpa.repository.support.JpaRepositoryFactory(entityManager)
                .getRepository(AuditEventRepository.class);
        workspaceService = mock(WorkspaceService.class);
        service = new AuditQueryService(repository, workspaceService);

        repository.save(new AuditEvent(1_000, "root", "ROLE_ADMIN", "ADMIN_VIEWED_WORKSPACE", "SUCCESS", "WORKSPACE", ws1.toString(), ws1, "1.1.1.1", null, null, "name=Acme"));
        repository.save(new AuditEvent(2_000, "boss", "ROLE_USER", "MEMBER_ADDED", "SUCCESS", "USER", "ahmet", ws1, "2.2.2.2", null, null, "role=MEMBER"));
        repository.save(new AuditEvent(3_000, "ahmet", "ROLE_USER", "ACCESS_DENIED", "DENIED", "LINK", "abc", ws1, "3.3.3.3", null, null, "attempted=canDeleteLink"));
        repository.save(new AuditEvent(4_000, "zed", "ROLE_USER", "MEMBER_ADDED", "SUCCESS", "USER", "kim", ws2, "4.4.4.4", null, null, null));
        repository.save(new AuditEvent(5_000, null, null, "LOGIN_FAILED", "FAILURE", "USER", "root", null, "5.5.5.5", null, null, "bad credentials"));
        entityManager.flush();
    }

    @Test
    void newestEventsComeFirst() {
        PagedResponse<AuditEventResponse> all = service.search(null, null, null, null, null, null, 0, 50);
        assertEquals(5, all.getTotalElements());
        assertEquals("LOGIN_FAILED", all.getContent().get(0).getAction());
        assertEquals("ADMIN_VIEWED_WORKSPACE", all.getContent().get(4).getAction());
    }

    @Test
    void filtersCombine() {
        assertEquals(2, service.search(null, "member_added", null, null, null, null, 0, 50).getTotalElements());
        assertEquals(1, service.search("ROOT", null, null, null, null, null, 0, 50).getTotalElements(), "actor match ignores case");
        assertEquals(1, service.search(null, null, "denied", null, null, null, 0, 50).getTotalElements());
        assertEquals(3, service.search(null, null, null, ws1, null, null, 0, 50).getTotalElements());
        assertEquals(2, service.search(null, null, null, null, 2_000L, 3_000L, 0, 50).getTotalElements());
        assertEquals(1, service.search(null, "MEMBER_ADDED", null, ws2, null, null, 0, 50).getTotalElements());
        assertEquals(0, service.search("nobody", null, null, null, null, null, 0, 50).getTotalElements());
    }

    @Test
    void pagesAndCapsTheSize() {
        PagedResponse<AuditEventResponse> page = service.search(null, null, null, null, null, null, 1, 2);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalPages());
        assertEquals(200, service.search(null, null, null, null, null, null, 0, 100_000).getSize());
    }

    @Test
    void aWorkspaceAdminOnlySeesTheirOwnWorkspaceAndMustBeAuthorised() {
        PagedResponse<AuditEventResponse> mine = service.searchWorkspace(ws1, null, null, null, null, 0, 50);

        verify(workspaceService).requireWorkspaceAdmin(ws1);
        assertEquals(3, mine.getTotalElements());
        assertTrue(mine.getContent().stream().allMatch(e -> ws1.equals(e.getWorkspaceId())));
        assertTrue(mine.getContent().stream().anyMatch(e -> "root".equals(e.getActorUsername())),
                "platform admin access to the customer's data is visible to the customer");
    }

    @Test
    void anUnauthorisedWorkspaceCallerGetsNothing() {
        when(workspaceService.requireWorkspaceAdmin(ws2)).thenThrow(new SecurityException("no"));
        assertThrows(SecurityException.class, () -> service.searchWorkspace(ws2, null, null, null, null, 0, 50));
    }
}
