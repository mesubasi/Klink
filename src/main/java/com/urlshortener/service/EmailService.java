package com.urlshortener.service;

import com.urlshortener.model.UrlMapping;
import jakarta.mail.internet.MimeMessage;
import org.springframework.web.util.HtmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:noreply@klink.local}")
    private String fromEmail;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Async
    public void sendBrokenLinkAlert(UrlMapping mapping) {
        if (mapping == null || mapping.getUser() == null || mapping.getUser().getEmail() == null || mapping.getUser().getEmail().trim().isEmpty()) {
            return;
        }

        String recipientEmail = mapping.getUser().getEmail().trim();
        String shortCode = mapping.getShortCode();
        String originalUrl = mapping.getOriginalUrl();
        String errorMessage = mapping.getHealthErrorMessage() != null ? mapping.getHealthErrorMessage() : "Hedef sunucuya ulaşılamadı";
        String timeStr = Instant.ofEpochMilli(mapping.getLastHealthCheck() != null ? mapping.getLastHealthCheck() : System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .format(FORMATTER);

        String subject = "🚨 [Klink Alarm] Kırık Link Tespiti: /" + shortCode;
        String htmlBody = buildBrokenLinkHtml(shortCode, originalUrl, errorMessage, timeStr);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("📧 [Simüle E-posta] Kırık link bildirimi hazırlandı (SMTP devre dışı): Kime: {}, Link: /{}, Hata: {}",
                    recipientEmail, shortCode, errorMessage);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("📧 [E-posta Gönderildi] Kırık link uyarı e-postası başarıyla iletildi: Kime: {}, Link: /{}", recipientEmail, shortCode);
        } catch (Exception e) {
            log.warn("⚠️ Kırık link uyarı e-postası gönderilemedi ({}/{}): {}", recipientEmail, shortCode, e.getMessage());
        }
    }

    /**
     * Sends a workspace invitation email synchronously.
     * Returns false when SMTP is not configured or sending fails, so callers can show the link to the inviter instead.
     */
    public boolean sendWorkspaceInvitation(String recipientEmail, String workspaceName, String inviterName,
                                           String role, String inviteUrl, long expiresAt) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("📧 [Simüle E-posta] Çalışma alanı daveti hazırlandı (SMTP devre dışı): Kime: {}, Alan: {}", recipientEmail, workspaceName);
            return false;
        }

        String expiry = Instant.ofEpochMilli(expiresAt).atZone(ZoneId.systemDefault()).format(FORMATTER);
        String htmlBody = "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; background-color: #f9f9fb; padding: 24px;'>"
                + "<div style='max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #e5e7eb; padding: 28px;'>"
                + "<h2 style='margin: 0 0 12px; font-size: 20px; color: #111827;'>Klink çalışma alanına davet edildiniz</h2>"
                + "<p style='color: #4b5563; font-size: 14px; line-height: 1.6;'><strong>" + HtmlUtils.htmlEscape(inviterName) + "</strong>, sizi <strong>"
                + HtmlUtils.htmlEscape(workspaceName) + "</strong> çalışma alanına <strong>" + HtmlUtils.htmlEscape(role) + "</strong> rolüyle davet etti.</p>"
                + "<p style='margin: 24px 0;'><a href='" + HtmlUtils.htmlEscape(inviteUrl) + "' style='background: #111827; color: #ffffff; padding: 12px 20px; border-radius: 10px; text-decoration: none; font-size: 14px;'>Daveti Kabul Et</a></p>"
                + "<p style='color: #6b7280; font-size: 12px;'>Bu davet " + expiry + " tarihine kadar geçerlidir ve yalnızca bu e-posta adresiyle kullanılabilir.</p>"
                + "<p style='color: #9ca3af; font-size: 11px; margin-top: 24px; text-align: center;'>Klink &copy; 2026</p>"
                + "</div></body></html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("Klink: " + workspaceName + " çalışma alanına davet edildiniz");
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("📧 [E-posta Gönderildi] Çalışma alanı daveti iletildi: Kime: {}, Alan: {}", recipientEmail, workspaceName);
            return true;
        } catch (Exception e) {
            log.warn("⚠️ Çalışma alanı daveti gönderilemedi ({}): {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    private String buildBrokenLinkHtml(String shortCode, String originalUrl, String errorMessage, String timeStr) {
        return "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; background-color: #f9f9fb; padding: 24px;'>"
                + "<div style='max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #fee2e2; padding: 28px;'>"
                + "<div style='display: flex; align-items: center; gap: 8px; margin-bottom: 16px;'>"
                + "<h2 style='color: #dc2626; margin: 0; font-size: 20px;'>🚨 Kırık Link Uyarısı</h2>"
                + "</div>"
                + "<p style='color: #4b5563; font-size: 13px; line-height: 1.5;'>"
                + "Klink otomatik sağlık denetçisi, aşağıdaki kısa bağlantınızın hedef web sitesine erişemedi ve linki <strong>BROKEN (Kırık)</strong> olarak işaretledi."
                + "</p>"
                + "<div style='background: #fef2f2; border: 1px solid #fecaca; border-radius: 12px; padding: 16px; margin: 20px 0;'>"
                + "<div style='font-size: 12px; color: #991b1b; font-weight: bold;'>Kısa Kod: <span style='font-family: monospace; font-size: 14px;'>/" + shortCode + "</span></div>"
                + "<div style='font-size: 12px; color: #7f1d1d; margin-top: 6px; word-break: break-all;'>Hedef: " + originalUrl + "</div>"
                + "<div style='font-size: 12px; color: #dc2626; margin-top: 8px; font-weight: bold;'>Hata Nedeni: " + errorMessage + "</div>"
                + "<div style='font-size: 11px; color: #9ca3af; margin-top: 6px;'>Tespit Zamanı: " + timeStr + "</div>"
                + "</div>"
                + "<p style='color: #6b7280; font-size: 12px;'>"
                + "Hedef siteniz düzeldiğinde veya yeni bir URL ile güncellediğinizde durum otomatik olarak güncellenecektir."
                + "</p>"
                + "<p style='color: #9ca3af; font-size: 11px; margin-top: 24px; text-align: center;'>Klink Reliability Engine &copy; 2026</p>"
                + "</div></body></html>";
    }
}
