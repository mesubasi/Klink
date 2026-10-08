package com.urlshortener;

import com.urlshortener.model.UserAccount;
import com.urlshortener.service.EmailVerificationPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationPolicyTest {

    private EmailVerificationPolicy policy(boolean required, String mailHost) {
        EmailVerificationPolicy p = new EmailVerificationPolicy();
        ReflectionTestUtils.setField(p, "required", required);
        ReflectionTestUtils.setField(p, "mailHost", mailHost);
        return p;
    }

    private UserAccount user(boolean verified, String role) {
        return UserAccount.builder().username("u").role(role).emailVerified(verified).build();
    }

    @Test
    void unverifiedUsersAreBlockedWhenEnforced() {
        assertThrows(SecurityException.class, () -> policy(true, "smtp.test").requireVerified(user(false, "ROLE_USER")));
    }

    @Test
    void verifiedUsersAndPlatformAdminsPass() {
        assertDoesNotThrow(() -> policy(true, "smtp.test").requireVerified(user(true, "ROLE_USER")));
        assertDoesNotThrow(() -> policy(true, "smtp.test").requireVerified(user(false, "ROLE_ADMIN")));
    }

    @Test
    void notEnforcedWhenDisabledOrWhenMailCannotBeDelivered() {
        assertDoesNotThrow(() -> policy(false, "smtp.test").requireVerified(user(false, "ROLE_USER")));
        assertDoesNotThrow(() -> policy(true, "").requireVerified(user(false, "ROLE_USER")),
                "without SMTP nobody could ever verify, so enforcing would lock everyone out");
    }
}
