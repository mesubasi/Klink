package com.urlshortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bot ve web crawler tespit servisi.
 * User-Agent başlığını analiz ederek gelen isteğin bir bot/crawler'dan mı
 * yoksa gerçek bir kullanıcıdan mı geldiğini belirler.
 *
 * Tespit edilen bot kategorileri:
 * - Arama motoru örümcekleri (Googlebot, Bingbot, Yandex, Baidu vb.)
 * - Sosyal medya önizleme botları (WhatsApp, Telegram, Twitter, Facebook, LinkedIn, Slack vb.)
 * - SEO ve monitoring araçları (Ahrefs, SEMrush, Screaming Frog vb.)
 * - HTTP kütüphaneleri ve otomasyon araçları (curl, wget, python-requests, Postman vb.)
 * - Güvenlik tarayıcıları ve yapay zeka crawlerları
 */
@Service
public class BotDetectorService {

    private static final Logger log = LoggerFactory.getLogger(BotDetectorService.class);

    /**
     * Bilinen bot/crawler User-Agent kalıpları.
     * Küçük harfe dönüştürülmüş User-Agent üzerinde contains kontrolü yapılır.
     */
    private static final List<String> BOT_PATTERNS = List.of(
            // Arama Motoru Botları (Search Engine Crawlers)
            "googlebot", "bingbot", "slurp", "duckduckbot", "baiduspider",
            "yandexbot", "yandexmobilebot", "sogou", "exabot", "ia_archiver",
            "archive.org_bot", "applebot", "petalbot", "seznambot", "mojeekbot",

            // Sosyal Medya Önizleme Botları (Social Media Preview Bots)
            "facebookexternalhit", "facebot", "twitterbot", "linkedinbot",
            "whatsapp", "telegrambot", "slackbot", "slack-imgproxy",
            "discordbot", "pinterestbot", "redditbot", "vkshare",
            "skypeuripreview", "viber", "tumblr",

            // SEO & Analitik Araçları
            "ahrefsbot", "semrushbot", "mj12bot", "dotbot", "rogerbot",
            "screaming frog", "serpstatbot", "sistrix", "blexbot",

            // HTTP İstemci Kütüphaneleri & Otomasyon Araçları
            "curl", "wget", "python-requests", "python-urllib", "httpie",
            "java/", "apache-httpclient", "okhttp", "go-http-client",
            "node-fetch", "axios", "postman", "insomnia",

            // Monitoring & Uptime Kontrol Botları
            "uptimerobot", "pingdom", "site24x7", "statuscake", "newrelicpinger",
            "datadog", "checkly", "better uptime",

            // Genel Bot İfadeleri
            "bot", "crawl", "spider", "scraper", "headless",

            // Yapay Zeka Crawlerları
            "gptbot", "chatgpt-user", "claudebot", "anthropic", "cohere-ai",
            "bytespider", "ccbot", "perplexitybot",

            // Güvenlik Tarayıcıları
            "nmap", "nikto", "sqlmap", "masscan", "zap"
    );

    /**
     * Daha spesifik pattern'lar için regex.
     * Genel "bot" kelimesi çok geniş olduğundan, bazı gerçek tarayıcıların
     * yanlış pozitif vermesini önlemek için whitelist kontrolü yapılır.
     */
    private static final List<String> HUMAN_BROWSER_INDICATORS = List.of(
            "mozilla", "chrome", "safari", "firefox", "edge", "opera", "vivaldi", "brave"
    );

    /**
     * Sadece genel bot/spider/crawler/scraper/headless kalıplarıdır.
     * Bu ifadeler User-Agent'ta geçiyorsa ve bilinen bir tarayıcı değilse → bot.
     */
    private static final List<String> GENERIC_BOT_KEYWORDS = List.of(
            "bot", "crawl", "spider", "scraper", "headless"
    );

    /**
     * Spesifik (arama motoru, sosyal medya vb.) bot kalıplarıdır.
     * Bu ifadeler User-Agent'ta geçerse kesin bot olarak kabul edilir.
     */
    private static final List<String> SPECIFIC_BOT_PATTERNS;

    static {
        SPECIFIC_BOT_PATTERNS = BOT_PATTERNS.stream()
                .filter(p -> !GENERIC_BOT_KEYWORDS.contains(p))
                .toList();
    }

    /**
     * Verilen User-Agent string'inin bir bot/crawler'a ait olup olmadığını belirler.
     *
     * @param userAgent HTTP User-Agent başlık değeri
     * @return true ise bot, false ise gerçek kullanıcı
     */
    public boolean isBot(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            // User-Agent olmadan gelen istekler genellikle script/bot'tur
            return true;
        }

        String ua = userAgent.toLowerCase();

        // 1. Spesifik bot kalıplarını kontrol et (kesin bot tespiti)
        for (String pattern : SPECIFIC_BOT_PATTERNS) {
            if (ua.contains(pattern)) {
                log.debug("Bot tespit edildi (spesifik kalıp: '{}'): {}", pattern, truncateForLog(userAgent));
                return true;
            }
        }

        // 2. Genel bot anahtar kelimeleri kontrol et (yanlış pozitif korumalı)
        for (String keyword : GENERIC_BOT_KEYWORDS) {
            if (ua.contains(keyword)) {
                // "Aboutbot" gibi durumlar için: gerçek bir tarayıcı değilse bot kabul et
                boolean looksLikeRealBrowser = HUMAN_BROWSER_INDICATORS.stream()
                        .anyMatch(indicator -> ua.contains(indicator) && !ua.contains(keyword + "/"));
                if (!looksLikeRealBrowser) {
                    log.debug("Bot tespit edildi (genel anahtar: '{}'): {}", keyword, truncateForLog(userAgent));
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Tespit edilen bot'un kategorisini döner.
     * Analitik raporlamada bot türünü göstermek için kullanılır.
     *
     * @param userAgent HTTP User-Agent başlık değeri
     * @return Bot kategorisi (örn: "Arama Motoru", "Sosyal Medya", "SEO Aracı") veya null
     */
    public String getBotCategory(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return "Bilinmeyen (Unknown)";
        }

        String ua = userAgent.toLowerCase();

        // Arama Motoru Botları
        if (containsAny(ua, "googlebot", "bingbot", "slurp", "duckduckbot", "baiduspider",
                "yandexbot", "yandexmobilebot", "sogou", "exabot", "applebot", "petalbot",
                "seznambot", "mojeekbot")) {
            return "Arama Motoru (Search Engine)";
        }

        // Sosyal Medya Önizleme Botları
        if (containsAny(ua, "facebookexternalhit", "facebook", "facebot", "twitterbot", "twitter", "linkedinbot", "linkedin",
                "whatsapp", "telegrambot", "telegram", "slackbot", "slack", "slack-imgproxy", "discordbot", "discord",
                "pinterestbot", "pinterest", "redditbot", "reddit", "vkshare", "skypeuripreview", "viber", "tumblr")) {
            return "Sosyal Medya (Social Media)";
        }

        // SEO & Analitik
        if (containsAny(ua, "ahrefsbot", "semrushbot", "mj12bot", "dotbot", "rogerbot",
                "screaming frog", "serpstatbot", "sistrix", "blexbot")) {
            return "SEO Aracı (SEO Tool)";
        }

        // HTTP İstemci Kütüphaneleri
        if (containsAny(ua, "curl", "wget", "python-requests", "python-urllib", "httpie",
                "java/", "apache-httpclient", "okhttp", "go-http-client", "node-fetch",
                "axios", "postman", "insomnia")) {
            return "HTTP İstemcisi (HTTP Client)";
        }

        // Monitoring
        if (containsAny(ua, "uptimerobot", "pingdom", "site24x7", "statuscake",
                "newrelicpinger", "datadog", "checkly", "better uptime")) {
            return "Monitoring Aracı (Monitoring)";
        }

        // AI Crawler
        if (containsAny(ua, "gptbot", "chatgpt-user", "claudebot", "anthropic",
                "cohere-ai", "bytespider", "ccbot", "perplexitybot")) {
            return "Yapay Zeka (AI Crawler)";
        }

        // Güvenlik Tarayıcısı
        if (containsAny(ua, "nmap", "nikto", "sqlmap", "masscan", "zap")) {
            return "Güvenlik Tarayıcısı (Security Scanner)";
        }

        // Arşiv
        if (containsAny(ua, "archive.org_bot", "ia_archiver")) {
            return "Arşiv (Archive)";
        }

        return "Diğer Bot (Other Bot)";
    }

    private boolean containsAny(String text, String... patterns) {
        for (String pattern : patterns) {
            if (text.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String truncateForLog(String value) {
        if (value == null) return "null";
        return value.length() > 100 ? value.substring(0, 100) + "..." : value;
    }
}
