package com.urlshortener.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.urlshortener.model.UserAccount;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String TOKEN_VERSION_CLAIM = "tv";

    /** The signing key that used to be the fallback in the repository's compose files and test config. */
    private static final String LEAKED_DEFAULT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final Environment environment;

    public JwtTokenProvider(Environment environment) {
        this.environment = environment;
    }

    /** Refuse to start in production with a weak key or the publicly known default one. */
    @PostConstruct
    public void validateSecret() {
        byte[] key;
        try {
            key = Decoders.BASE64.decode(jwtSecret);
        } catch (RuntimeException e) {
            throw new IllegalStateException("JWT_SECRET must be a base64 string (e.g. `openssl rand -base64 32`).", e);
        }
        if (key.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 256 bits (32 bytes).");
        }
        if (environment.acceptsProfiles(Profiles.of("prod")) && LEAKED_DEFAULT_SECRET.equalsIgnoreCase(jwtSecret.trim())) {
            throw new IllegalStateException("JWT_SECRET is the publicly known example key from the repository. Generate your own (`openssl rand -base64 32`).");
        }
    }

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Issues a token bound to the user's current {@code tokenVersion}; a later bump revokes it. */
    public String generateToken(UserAccount user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(TOKEN_VERSION_CLAIM, user.getTokenVersion())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /** Tokens issued before versioning existed have no claim and count as version 0. */
    public int getTokenVersionFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Integer version = claims.get(TOKEN_VERSION_CLAIM, Integer.class);
        return version != null ? version : 0;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Geçersiz veya süresi dolmuş JWT Token: {}", e.getMessage());
        }
        return false;
    }
}
