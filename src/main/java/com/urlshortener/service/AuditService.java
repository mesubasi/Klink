package com.urlshortener.service;

import com.urlshortener.model.AuditAction;
import com.urlshortener.model.AuditEvent;
import com.urlshortener.repository.AuditEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Writes the audit trail. Events are stored in their own transaction so that a refused or rolled-back
 * operation is still recorded, and a failure to record never breaks the operation itself (it is logged loudly).
 * Never pass secrets (passwords, tokens) as {@code details}.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String DENIED = "DENIED";

    private final AuditEventRepository repository;
    private final TransactionTemplate independentTransaction;

    @Value("${app.audit.retention-days:365}")
    private int retentionDays;

    public AuditService(AuditEventRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.independentTransaction = new TransactionTemplate(transactionManager);
        this.independentTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Records a successful action by the currently logged-in user. */
    public void record(AuditAction action, String targetType, String targetId, UUID workspaceId, String details) {
        write(currentUsername(), currentRole(), action.name(), SUCCESS, targetType, targetId, workspaceId, details);
    }

    /** Records an action that is not (yet) tied to a security context, e.g. a login. */
    public void recordAs(String username, String role, AuditAction action, String outcome,
                         String targetType, String targetId, UUID workspaceId, String details) {
        write(username, role, action.name(), outcome, targetType, targetId, workspaceId, details);
    }

    /** Records that the current user tried to do something they are not allowed to. */
    public void denied(AuditAction attempted, String targetType, String targetId, UUID workspaceId) {
        denied(attempted.name(), targetType, targetId, workspaceId);
    }

    /** @param what the operation that was refused, as free text (e.g. an action code or "workspace admin required") */
    public void denied(String what, String targetType, String targetId, UUID workspaceId) {
        write(currentUsername(), currentRole(), AuditAction.ACCESS_DENIED.name(), DENIED, targetType, targetId, workspaceId,
                "attempted=" + what);
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void purgeExpired() {
        if (retentionDays <= 0) {
            return;
        }
        long cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays);
        Integer removed = independentTransaction.execute(status -> repository.deleteOlderThan(cutoff));
        if (removed != null && removed > 0) {
            log.info("{} denetim kaydı saklama süresi ({} gün) dolduğu için silindi.", removed, retentionDays);
        }
    }

    private void write(String username, String role, String action, String outcome, String targetType,
                       String targetId, UUID workspaceId, String details) {
        try {
            HttpServletRequest request = currentRequest();
            AuditEvent event = new AuditEvent(
                    System.currentTimeMillis(),
                    clip(username, 50),
                    clip(role, 20),
                    action,
                    outcome,
                    clip(targetType, 30),
                    clip(targetId, 100),
                    workspaceId,
                    request != null ? clip(request.getRemoteAddr(), 64) : null,
                    request != null ? clip(request.getHeader("X-Forwarded-For"), 255) : null,
                    request != null ? clip(request.getHeader("User-Agent"), 255) : null,
                    clip(details, 1000));
            independentTransaction.executeWithoutResult(status -> repository.save(event));
        } catch (Exception e) {
            // The operation that triggered the event must not fail because the trail could not be written.
            log.error("DENETİM KAYDI YAZILAMADI (action={}, actor={}): {}", action, username, e.getMessage());
        }
    }

    private static HttpServletRequest currentRequest() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs ? attrs.getRequest() : null;
    }

    private static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getName();
    }

    private static String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_")).findFirst().orElse(null);
    }

    /** Trims, strips control characters (log forging) and truncates. */
    static String clip(String value, int max) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replaceAll("[\\p{Cntrl}]", " ").trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned.length() <= max ? cleaned : cleaned.substring(0, max);
    }
}
