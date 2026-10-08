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
        return urlMappingRepository.searchByUsername(username, pattern,
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
}
