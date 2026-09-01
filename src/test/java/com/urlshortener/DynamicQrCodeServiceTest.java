package com.urlshortener;

import com.urlshortener.service.DynamicQrCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicQrCodeServiceTest {

    private DynamicQrCodeService dynamicQrCodeService;

    @BeforeEach
    public void setup() {
        dynamicQrCodeService = new DynamicQrCodeService();
    }

    @Test
    public void testGenerateDefaultQrCodePng() {
        byte[] png = dynamicQrCodeService.generateCustomQrCodePng(
                "https://klink.to/test123", 512, 512, "#000000", "#ffffff", null, "square", null);
        assertNotNull(png);
        assertTrue(png.length > 500);
    }

    @Test
    public void testGenerateStyledQrCodeWithDotsAndColors() {
        byte[] png = dynamicQrCodeService.generateCustomQrCodePng(
                "https://klink.to/summer-sale", 512, 512, "#3b82f6", "#f8fafc", "#10b981", "dots", null);
        assertNotNull(png);
        assertTrue(png.length > 500);
    }

    @Test
    public void testGenerateQrCodeSvg() {
        String svg = dynamicQrCodeService.generateCustomQrCodeSvg(
                "https://klink.to/vector-link", 512, "#6366f1", "#ffffff", "#ec4899", "rounded");
        assertNotNull(svg);
        assertTrue(svg.contains("<svg"));
        assertTrue(svg.contains("</svg>"));
        assertTrue(svg.contains("fill=\"#6366f1\""));
    }
}
