package com.urlshortener;

import com.urlshortener.service.DynamicQrCodeService;
import com.urlshortener.util.SecurityUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityHardeningTest {

    @Test
    public void testSsrfProtectionBlocksPrivateIps() {
        assertFalse(SecurityUtils.isPubliclyAccessibleUrl("http://localhost:8080/admin"));
        assertFalse(SecurityUtils.isPubliclyAccessibleUrl("http://127.0.0.1:5432"));
        assertFalse(SecurityUtils.isPubliclyAccessibleUrl("http://169.254.169.254/latest/meta-data"));
        assertFalse(SecurityUtils.isPubliclyAccessibleUrl("http://0.0.0.0:80"));
        assertFalse(SecurityUtils.isPubliclyAccessibleUrl("http://10.0.0.1/internal"));
        assertFalse(SecurityUtils.isPubliclyAccessibleUrl("http://192.168.1.1/router"));
    }

    @Test
    public void testSsrfProtectionAllowsPublicUrls() {
        assertTrue(SecurityUtils.isPubliclyAccessibleUrl("https://google.com"));
        assertTrue(SecurityUtils.isPubliclyAccessibleUrl("https://github.com/spring-projects/spring-boot"));
    }

    @Test
    public void testSvgAttributeInjectionSanitization() {
        DynamicQrCodeService service = new DynamicQrCodeService();
        // Malicious SVG injection string in hex parameter
        String maliciousColor = "#000\" onclick=\"alert('XSS')\" foo=\"";
        String svg = service.generateCustomQrCodeSvg("https://klink.to/test", 512, maliciousColor, "#ffffff", null, "square");

        assertNotNull(svg);
        assertFalse(svg.contains("onclick"));
        assertFalse(svg.contains("alert"));
        assertTrue(svg.contains("fill=\"#000000\"")); // Defaults to safe hex #000000
    }
}
