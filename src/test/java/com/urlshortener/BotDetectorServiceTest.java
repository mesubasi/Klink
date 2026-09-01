package com.urlshortener;

import com.urlshortener.service.BotDetectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BotDetectorServiceTest {

    private BotDetectorService botDetectorService;

    @BeforeEach
    void setUp() {
        botDetectorService = new BotDetectorService();
    }

    // ==================== Gerçek Tarayıcı Testleri (Bot Olmamalı) ====================

    @ParameterizedTest
    @DisplayName("Gerçek tarayıcı User-Agent'ları bot olarak tespit edilmemeli")
    @ValueSource(strings = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1"
    })
    void shouldNotDetectRealBrowsersAsBots(String userAgent) {
        assertFalse(botDetectorService.isBot(userAgent),
                "Gerçek tarayıcı bot olarak tespit edilmemeli: " + userAgent);
    }

    // ==================== Arama Motoru Bot Testleri ====================

    @ParameterizedTest
    @DisplayName("Arama motoru botları doğru tespit edilmeli")
    @ValueSource(strings = {
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
            "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
            "Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)",
            "DuckDuckBot/1.1; (+http://duckduckgo.com/duckduckbot.html)",
            "Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)"
    })
    void shouldDetectSearchEngineBots(String userAgent) {
        assertTrue(botDetectorService.isBot(userAgent),
                "Arama motoru botu tespit edilmeli: " + userAgent);
        assertEquals("Arama Motoru (Search Engine)", botDetectorService.getBotCategory(userAgent));
    }

    // ==================== Sosyal Medya Bot Testleri ====================

    @ParameterizedTest
    @DisplayName("Sosyal medya önizleme botları doğru tespit edilmeli")
    @ValueSource(strings = {
            "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)",
            "Twitterbot/1.0",
            "LinkedInBot/1.0 (compatible; Mozilla/5.0; Apache-HttpClient +http://www.linkedin.com)",
            "WhatsApp/2.23.20.0 A",
            "TelegramBot (like TwitterBot)",
            "Slackbot-LinkExpanding 1.0 (+https://api.slack.com/robots)",
            "Mozilla/5.0 (compatible; Discordbot/2.0; +https://discordapp.com)"
    })
    void shouldDetectSocialMediaBots(String userAgent) {
        assertTrue(botDetectorService.isBot(userAgent),
                "Sosyal medya botu tespit edilmeli: " + userAgent);
        assertEquals("Sosyal Medya (Social Media)", botDetectorService.getBotCategory(userAgent));
    }

    // ==================== SEO Araç Testleri ====================

    @ParameterizedTest
    @DisplayName("SEO araç botları doğru tespit edilmeli")
    @ValueSource(strings = {
            "Mozilla/5.0 (compatible; AhrefsBot/7.0; +http://ahrefs.com/robot/)",
            "Mozilla/5.0 (compatible; SemrushBot/7~bl; +http://www.semrush.com/bot.html)",
            "Mozilla/5.0 (compatible; MJ12bot/v1.4.8; http://mj12bot.com/)"
    })
    void shouldDetectSeoBots(String userAgent) {
        assertTrue(botDetectorService.isBot(userAgent),
                "SEO botu tespit edilmeli: " + userAgent);
        assertEquals("SEO Aracı (SEO Tool)", botDetectorService.getBotCategory(userAgent));
    }

    // ==================== HTTP İstemci Testleri ====================

    @ParameterizedTest
    @DisplayName("HTTP istemci kütüphaneleri doğru tespit edilmeli")
    @ValueSource(strings = {
            "curl/8.4.0",
            "Wget/1.21.4",
            "python-requests/2.31.0",
            "PostmanRuntime/7.36.0",
            "axios/1.6.3",
            "okhttp/4.12.0",
            "Go-http-client/2.0"
    })
    void shouldDetectHttpClients(String userAgent) {
        assertTrue(botDetectorService.isBot(userAgent),
                "HTTP istemcisi tespit edilmeli: " + userAgent);
        assertEquals("HTTP İstemcisi (HTTP Client)", botDetectorService.getBotCategory(userAgent));
    }

    // ==================== AI Crawler Testleri ====================

    @ParameterizedTest
    @DisplayName("Yapay zeka crawlerları doğru tespit edilmeli")
    @ValueSource(strings = {
            "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; GPTBot/1.0; +https://openai.com/gptbot)",
            "ClaudeBot/1.0",
            "CCBot/2.0 (https://commoncrawl.org/faq/)"
    })
    void shouldDetectAiCrawlers(String userAgent) {
        assertTrue(botDetectorService.isBot(userAgent),
                "AI crawler tespit edilmeli: " + userAgent);
        assertEquals("Yapay Zeka (AI Crawler)", botDetectorService.getBotCategory(userAgent));
    }

    // ==================== Monitoring Araç Testleri ====================

    @Test
    @DisplayName("Monitoring araçları doğru tespit edilmeli")
    void shouldDetectMonitoringTools() {
        String ua = "Mozilla/5.0 (compatible; UptimeRobot/2.0; http://www.uptimerobot.com/)";
        assertTrue(botDetectorService.isBot(ua));
        assertEquals("Monitoring Aracı (Monitoring)", botDetectorService.getBotCategory(ua));
    }

    // ==================== Null / Empty User-Agent Testleri ====================

    @ParameterizedTest
    @DisplayName("Null veya boş User-Agent bot olarak işaretlenmeli")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void shouldTreatNullOrEmptyAsBot(String userAgent) {
        assertTrue(botDetectorService.isBot(userAgent),
                "Boş veya null User-Agent bot olarak değerlendirilmeli");
    }

    // ==================== Bot Kategorisi Testleri ====================

    @Test
    @DisplayName("Null User-Agent için kategori 'Bilinmeyen' olmalı")
    void shouldReturnUnknownCategoryForNull() {
        assertEquals("Bilinmeyen (Unknown)", botDetectorService.getBotCategory(null));
    }

    @Test
    @DisplayName("Boş User-Agent için kategori 'Bilinmeyen' olmalı")
    void shouldReturnUnknownCategoryForEmpty() {
        assertEquals("Bilinmeyen (Unknown)", botDetectorService.getBotCategory(""));
    }

    // ==================== Güvenlik Tarayıcısı Testleri ====================

    @Test
    @DisplayName("Güvenlik tarayıcıları doğru tespit edilmeli")
    void shouldDetectSecurityScanners() {
        assertTrue(botDetectorService.isBot("Nikto/2.1.6"));
        assertEquals("Güvenlik Tarayıcısı (Security Scanner)", botDetectorService.getBotCategory("Nikto/2.1.6"));
    }
}
