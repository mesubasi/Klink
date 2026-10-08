package com.urlshortener;

import com.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UrlMapping.user and UrlMapping.workspace are lazy. With open-in-view off (production) any service method that reads
 * them -- permission checks, audit entries, responses -- must run inside a transaction, otherwise it fails with a
 * LazyInitializationException (HTTP 500) that unit tests with mocks and the dev profile never show.
 */
class LazyLoadingGuardTest {

    private static final List<String> METHODS_TOUCHING_LINK_OWNER_OR_WORKSPACE = List.of(
            "getAnalytics", "getAnalyticsSummary", "exportAnalyticsReport", "sendWeeklyEmailReport",
            "checkHealth", "checkAllMyUrlsHealth", "toggleUrlStatus", "deleteUrl",
            "getAllUrls", "getMyUrls", "searchMyUrls");

    @Test
    void methodsThatReadTheOwnerOrWorkspaceOfALinkAreTransactional() {
        for (String name : METHODS_TOUCHING_LINK_OWNER_OR_WORKSPACE) {
            List<Method> candidates = Arrays.stream(UrlShortenerService.class.getDeclaredMethods())
                    .filter(m -> m.getName().equals(name)).toList();
            assertTrue(!candidates.isEmpty(), "method renamed? " + name);
            for (Method m : candidates) {
                assertNotNull(m.getAnnotation(Transactional.class),
                        name + " reads lazy associations of UrlMapping and needs @Transactional (fails with 500 in production otherwise)");
            }
        }
    }
}
