package com.urlshortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
public class GeoIpService {

    private static final Logger log = LoggerFactory.getLogger(GeoIpService.class);

    public static class GeoLocation {
        private final String country;
        private final String countryCode;
        private final String city;

        public GeoLocation(String country, String countryCode, String city) {
            this.country = country;
            this.countryCode = countryCode;
            this.city = city;
        }

        public String getCountry() { return country; }
        public String getCountryCode() { return countryCode; }
        public String getCity() { return city; }
    }

    public GeoLocation resolveLocation(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return new GeoLocation("Bilinmiyor (Unknown)", "XX", "Bilinmiyor (Unknown)");
        }

        String ip = ipAddress.trim();

        // Localhost / Internal IP Range Check
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return new GeoLocation("Türkiye (Turkey)", "TR", "İstanbul");
        }

        // Demo Geo-IP Mock IP ranges
        if (ip.startsWith("8.8.") || ip.startsWith("35.") || ip.startsWith("34.")) {
            return new GeoLocation("Amerika Birleşik Devletleri (United States)", "US", "Mountain View");
        }

        if (ip.startsWith("1.1.") || ip.startsWith("13.")) {
            return new GeoLocation("Avustralya (Australia)", "AU", "Sydney");
        }

        if (ip.startsWith("185.") || ip.startsWith("176.") || ip.startsWith("212.")) {
            return new GeoLocation("Türkiye (Turkey)", "TR", "Ankara");
        }

        if (ip.startsWith("82.") || ip.startsWith("80.")) {
            return new GeoLocation("Almanya (Germany)", "DE", "Frankfurt");
        }

        if (ip.startsWith("109.") || ip.startsWith("77.")) {
            return new GeoLocation("Birleşik Krallık (United Kingdom)", "GB", "Londra");
        }

        // Default Fallback
        return new GeoLocation("Türkiye (Turkey)", "TR", "İstanbul");
    }
}
