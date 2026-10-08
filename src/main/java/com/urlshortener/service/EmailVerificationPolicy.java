package com.urlshortener.service;

import com.urlshortener.model.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sensitive actions (creating a workspace, joining one through an invitation) can require a verified email.
 * Controlled by {@code app.auth.require-email-verification} and only enforced when SMTP is configured;
 * platform admins are always exempt.
 */
@Component
public class EmailVerificationPolicy {

    @Value("${app.auth.require-email-verification:false}")
    private boolean required;

    /** Verification mails can only be delivered when SMTP is configured; enforcing it otherwise would lock everyone out. */
    @Value("${spring.mail.host:}")
    private String mailHost;

    public void requireVerified(UserAccount user) {
        if (!required || mailHost == null || mailHost.isBlank() || user == null || user.isEmailVerified() || "ROLE_ADMIN".equals(user.getRole())) {
            return;
        }
        throw new SecurityException("Bu işlem için e-posta adresinizi doğrulamanız gerekiyor. Size gönderilen doğrulama e-postasındaki bağlantıyı açın.");
    }
}
