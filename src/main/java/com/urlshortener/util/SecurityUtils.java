package com.urlshortener.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.util.Set;

public class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    private static final Set<String> BLOCKED_HOSTNAMES = Set.of(
            "localhost",
            "127.0.0.1",
            "::1",
            "0.0.0.0",
            "metadata.google.internal",
            "instance-data"
    );

    /**
     * Verilen URL'nin yerel ağ, loopback veya bulut meta-veri (SSRF) adreslerine
     * işaret edip etmediğini kontrol eder.
     *
     * @param url Kontrol edilecek web adresi
     * @return true ise URL güvenli (genel internete açık), false ise SSRF riski taşıyor
     */
    public static boolean isPubliclyAccessibleUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            URI uri = URI.create(url.trim());
            String host = uri.getHost();
            if (host == null || host.trim().isEmpty()) {
                return false;
            }

            String lowerHost = host.toLowerCase().trim();
            if (BLOCKED_HOSTNAMES.contains(lowerHost)) {
                return false;
            }

            // AWS / GCP / Azure Instance Metadata Service (IMDS) IP'si
            if (lowerHost.startsWith("169.254.") || lowerHost.equals("169.254.169.254")) {
                return false;
            }

            // IP Çözümlemesi ve Özel Aralık (RFC 1918) Kontrolü
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (address.isLoopbackAddress() ||
                    address.isAnyLocalAddress() ||
                    address.isLinkLocalAddress() ||
                    address.isSiteLocalAddress() ||
                    address.isMulticastAddress()) {
                    log.warn("🚨 [SSRF Koruması] Özel/Yerel IP adresi tespit edildi ve engellendi: {} -> {}", url, address.getHostAddress());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.debug("SSRF kontrolü sırasında alan adı çözümlenemedi (Geçerli veya uzak host): {}", e.getMessage());
            // DNS çözümlenemiyorsa (örn. henüz kaydedilmemiş veya sahte domain), yerel ağ riski yoktur
            return true;
        }
    }
}
