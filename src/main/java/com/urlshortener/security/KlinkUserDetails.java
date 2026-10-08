package com.urlshortener.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/** Spring user that also carries the account's current token version, checked against the JWT claim. */
public class KlinkUserDetails extends User {

    private final int tokenVersion;

    public KlinkUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, int tokenVersion) {
        super(username, password, authorities);
        this.tokenVersion = tokenVersion;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }
}
