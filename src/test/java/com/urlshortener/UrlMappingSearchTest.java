package com.urlshortener;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UrlMappingSearchTest {

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        UserAccount alice = userRepository.save(UserAccount.builder()
                .username("alice").email("alice@example.com").password("x").role("USER")
                .createdAt(1L).build());
        UserAccount bob = userRepository.save(UserAccount.builder()
                .username("bob").email("bob@example.com").password("x").role("USER")
                .createdAt(1L).build());

        save(alice, "promo1", "https://example.com/summer-sale", 1L);
        save(alice, "docs", "https://docs.example.org/guide", 2L);
        save(alice, "pct_100", "https://example.com/100%25", 3L);
        save(bob, "promo2", "https://example.com/bob", 4L);
    }

    private void save(UserAccount user, String code, String url, long createdAt) {
        urlMappingRepository.save(UrlMapping.builder()
                .originalUrl(url).shortCode(code).createdAt(createdAt).user(user).build());
    }

    private Page<UrlMapping> search(String username, String pattern, int page, int size) {
        return urlMappingRepository.searchByUsername(username, pattern, "ALL",
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Test
    void emptyPatternReturnsOnlyOwnLinks() {
        Page<UrlMapping> result = search("alice", "%%", 0, 10);
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void matchesShortCodeAndOriginalUrlCaseInsensitively() {
        assertEquals(1, search("alice", "%promo%", 0, 10).getTotalElements());
        assertEquals(1, search("alice", "%guide%", 0, 10).getTotalElements());
        assertEquals(2, search("alice", "%example.com%", 0, 10).getTotalElements());
    }

    @Test
    void escapedWildcardsAreTreatedAsLiterals() {
        assertEquals(1, search("alice", "%\\_%", 0, 10).getTotalElements());
        assertEquals(1, search("alice", "%\\%%", 0, 10).getTotalElements());
    }

    @Test
    void paginatesAndSortsNewestFirst() {
        Page<UrlMapping> first = search("alice", "%%", 0, 2);
        assertEquals(2, first.getContent().size());
        assertEquals(2, first.getTotalPages());
        assertEquals("pct_100", first.getContent().get(0).getShortCode());
        assertEquals(1, search("alice", "%%", 1, 2).getContent().size());
    }

    @Test
    void filtersByProtectionAndHealth() {
        UserAccount carol = userRepository.save(UserAccount.builder()
                .username("carol").email("carol@example.com").password("x").role("USER")
                .createdAt(1L).build());
        urlMappingRepository.save(UrlMapping.builder().originalUrl("https://a.example").shortCode("locked")
                .createdAt(1L).passwordHash("hash").user(carol).build());
        urlMappingRepository.save(UrlMapping.builder().originalUrl("https://b.example").shortCode("dead")
                .createdAt(2L).healthStatus("BROKEN").user(carol).build());
        urlMappingRepository.save(UrlMapping.builder().originalUrl("https://c.example").shortCode("ok")
                .createdAt(3L).healthStatus("HEALTHY").clickCount(5L).user(carol).build());

        PageRequest paging = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        assertEquals(1, urlMappingRepository.searchByUsername("carol", "%%", "PROTECTED", paging).getTotalElements());
        assertEquals(1, urlMappingRepository.searchByUsername("carol", "%%", "BROKEN", paging).getTotalElements());
        assertEquals(3, urlMappingRepository.searchByUsername("carol", "%%", "ALL", paging).getTotalElements());

        com.urlshortener.dto.LinkStatsResponse stats = urlMappingRepository.getStatsByUsername("carol");
        assertEquals(3, stats.getTotalLinks());
        assertEquals(5, stats.getTotalClicks());
        assertEquals(1, stats.getProtectedCount());
        assertEquals(1, stats.getBrokenCount());
        assertEquals(1, stats.getHealthyCount());
    }

    @Test
    void statsForUserWithoutLinksAreZero() {
        com.urlshortener.dto.LinkStatsResponse stats = urlMappingRepository.getStatsByUsername("nobody");
        assertEquals(0, stats.getTotalLinks());
        assertEquals(0, stats.getTotalClicks());
    }
}
