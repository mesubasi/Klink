package com.urlshortener;

import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtAuthenticationFilter;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.service.CustomUserDetailsService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtRevocationTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]).replace("AAAA", "QkJC");
    private static final String LEAKED = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtTokenProvider provider;
    private UserRepository userRepository;
    private JwtAuthenticationFilter filter;
    private UserAccount user;

    private JwtTokenProvider providerWith(String secret, String... profiles) {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(profiles);
        JwtTokenProvider p = new JwtTokenProvider(env);
        ReflectionTestUtils.setField(p, "jwtSecret", secret);
        ReflectionTestUtils.setField(p, "jwtExpirationMs", 3_600_000L);
        return p;
    }

    @BeforeEach
    void setUp() {
        provider = providerWith(SECRET);
        userRepository = mock(UserRepository.class);
        filter = new JwtAuthenticationFilter(provider, new CustomUserDetailsService(userRepository));
        user = UserAccount.builder().id(UUID.randomUUID()).username("ayse").email("a@a.com").password("pw").role("ROLE_USER").build();
        when(userRepository.findByUsername("ayse")).thenReturn(Optional.of(user));
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private boolean authenticates(String jwt) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwt);
        SecurityContextHolder.clearContext();
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        return SecurityContextHolder.getContext().getAuthentication() != null
                && "ayse".equals(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void tokenCarriesTheAccountVersion() {
        user.setTokenVersion(3);
        String jwt = provider.generateToken(user);

        assertTrue(provider.validateToken(jwt));
        assertEquals("ayse", provider.getUsernameFromJwt(jwt));
        assertEquals(3, provider.getTokenVersionFromJwt(jwt));
    }

    @Test
    void currentTokensAuthenticateRevokedOnesDoNot() throws Exception {
        String before = provider.generateToken(user);
        assertTrue(authenticates(before));

        user.revokeAllTokens();

        assertFalse(authenticates(before), "token issued before the password reset / sign-out-everywhere");
        assertTrue(authenticates(provider.generateToken(user)), "a fresh login after the bump works");
    }

    @Test
    void tokensIssuedBeforeVersioningKeepWorkingUntilTheFirstBump() throws Exception {
        String legacy = Jwts.builder()
                .subject("ayse")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertEquals(0, provider.getTokenVersionFromJwt(legacy));
        assertTrue(authenticates(legacy));

        user.revokeAllTokens();
        assertFalse(authenticates(legacy));
    }

    @Test
    void tamperedAndForeignTokensAreRejected() throws Exception {
        String jwt = provider.generateToken(user);
        String tampered = jwt.substring(0, jwt.length() - 3) + (jwt.endsWith("AAA") ? "BBB" : "AAA");
        assertFalse(authenticates(tampered));

        JwtTokenProvider other = providerWith(Base64.getEncoder().encodeToString("another-256-bit-secret-another-256".getBytes()));
        assertFalse(authenticates(other.generateToken(user)));
    }

    @Test
    void productionRefusesWeakOrPubliclyKnownKeys() {
        assertThrows(IllegalStateException.class, () -> providerWith(LEAKED, "prod").validateSecret());
        assertThrows(IllegalStateException.class, () -> providerWith(Base64.getEncoder().encodeToString(new byte[16]), "prod").validateSecret());
        assertThrows(IllegalStateException.class, () -> providerWith("not base64 !!!", "prod").validateSecret());
        assertDoesNotThrow(() -> providerWith(SECRET, "prod").validateSecret());
    }

    @Test
    void theExampleKeyIsStillAcceptedOutsideProduction() {
        assertDoesNotThrow(() -> providerWith(LEAKED).validateSecret(), "tests and local development keep working");
    }
}
