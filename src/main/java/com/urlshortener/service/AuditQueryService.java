package com.urlshortener.service;

import com.urlshortener.dto.AuditEventResponse;
import com.urlshortener.dto.PagedResponse;
import com.urlshortener.model.AuditEvent;
import com.urlshortener.repository.AuditEventRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Read side of the audit trail. Callers are responsible for authorising the scope they pass in. */
@Service
public class AuditQueryService {

    private static final int MAX_PAGE_SIZE = 200;

    private final AuditEventRepository repository;
    private final WorkspaceService workspaceService;

    public AuditQueryService(AuditEventRepository repository, WorkspaceService workspaceService) {
        this.repository = repository;
        this.workspaceService = workspaceService;
    }

    /** Platform-wide search for platform admins. */
    @Transactional(readOnly = true)
    public PagedResponse<AuditEventResponse> search(String actor, String action, String outcome, UUID workspaceId,
                                                    Long from, Long to, int page, int size) {
        return query(actor, action, outcome, workspaceId, from, to, page, size);
    }

    /** A workspace's own trail, for its admins (and platform admins). */
    @Transactional(readOnly = true)
    public PagedResponse<AuditEventResponse> searchWorkspace(UUID workspaceId, String actor, String action,
                                                             Long from, Long to, int page, int size) {
        workspaceService.requireWorkspaceAdmin(workspaceId);
        return query(actor, action, null, workspaceId, from, to, page, size);
    }

    private PagedResponse<AuditEventResponse> query(String actor, String action, String outcome, UUID workspaceId,
                                                    Long from, Long to, int page, int size) {
        Specification<AuditEvent> spec = (root, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (notBlank(actor)) p.add(cb.equal(cb.lower(root.get("actorUsername")), actor.trim().toLowerCase()));
            if (notBlank(action)) p.add(cb.equal(root.get("action"), action.trim().toUpperCase()));
            if (notBlank(outcome)) p.add(cb.equal(root.get("outcome"), outcome.trim().toUpperCase()));
            if (workspaceId != null) p.add(cb.equal(root.get("workspaceId"), workspaceId));
            if (from != null) p.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
            if (to != null) p.add(cb.lessThanOrEqualTo(root.get("occurredAt"), to));
            return cb.and(p.toArray(new Predicate[0]));
        };

        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Page<AuditEvent> result = repository.findAll(spec,
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "occurredAt")));
        List<AuditEventResponse> content = result.getContent().stream().map(AuditEventResponse::from).collect(Collectors.toList());
        return new PagedResponse<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
