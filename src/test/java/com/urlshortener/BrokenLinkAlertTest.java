package com.urlshortener;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.UserAccount;
import com.urlshortener.service.EmailService;
import com.urlshortener.service.EmailService.BrokenLinkAlert;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/** The alert mail runs on another thread, so it must carry plain values and never a JPA entity. */
class BrokenLinkAlertTest {

    @Test
    void copiesEverythingTheMailNeedsOnTheCallingThread() {
        UserAccount owner = UserAccount.builder().username("ayse").email("  ayse@a.com ").build();
        UrlMapping mapping = UrlMapping.builder().shortCode("promo").originalUrl("https://example.com/p").user(owner).build();
        mapping.setHealthErrorMessage("HTTP 503");
        mapping.setLastHealthCheck(1_700_000_000_000L);

        BrokenLinkAlert alert = BrokenLinkAlert.from(mapping);

        assertEquals("ayse@a.com", alert.recipientEmail());
        assertEquals("promo", alert.shortCode());
        assertEquals("https://example.com/p", alert.originalUrl());
        assertEquals("HTTP 503", alert.errorMessage());
        assertEquals(1_700_000_000_000L, alert.checkedAt());
    }

    @Test
    void noAlertWithoutAnOwnerEmail() {
        assertNull(BrokenLinkAlert.from(null));
        assertNull(BrokenLinkAlert.from(UrlMapping.builder().shortCode("x").originalUrl("https://e.com").build()));
        UserAccount noEmail = UserAccount.builder().username("u").email(" ").build();
        assertNull(BrokenLinkAlert.from(UrlMapping.builder().shortCode("x").originalUrl("https://e.com").user(noEmail).build()));
    }

    @Test
    void theAsyncSenderOnlyAcceptsPlainValues() throws Exception {
        Method sender = EmailService.class.getMethod("sendBrokenLinkAlert", BrokenLinkAlert.class);
        assertNotNull(sender.getAnnotation(Async.class));
        assertThrows(NoSuchMethodException.class, () -> EmailService.class.getMethod("sendBrokenLinkAlert", UrlMapping.class),
                "an entity must not be handed to another thread");
    }
}
