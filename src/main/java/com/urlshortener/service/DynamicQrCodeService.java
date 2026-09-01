package com.urlshortener.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class DynamicQrCodeService {

    private static final Logger log = LoggerFactory.getLogger(DynamicQrCodeService.class);

    public byte[] generateCustomQrCodePng(
            String content,
            int width,
            int height,
            String fgColorHex,
            String bgColorHex,
            String eyeColorHex,
            String dotStyle,
            String logoBase64) {
        try {
            int size = Math.max(width, height);
            if (size < 100) size = 400;
            if (size > 4096) size = 4096;

            Color fgColor = parseColor(fgColorHex, Color.BLACK);
            Color bgColor = parseColor(bgColorHex, Color.WHITE);
            Color eyeColor = parseColor(eyeColorHex, fgColor);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // Hata düzeltme seviyesi H (%30 kayıp toleransı): Merkezde logo varken bile sorunsuz taranır
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            int matrixWidth = bitMatrix.getWidth();
            int matrixHeight = bitMatrix.getHeight();

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();

            // Antialiasing & rendering kalitesi
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Arka planı boya
            g2.setColor(bgColor);
            g2.fillRect(0, 0, size, size);

            double cellWidth = (double) size / matrixWidth;
            double cellHeight = (double) size / matrixHeight;

            // QR Matrisini çiz
            for (int x = 0; x < matrixWidth; x++) {
                for (int y = 0; y < matrixHeight; y++) {
                    if (bitMatrix.get(x, y)) {
                        boolean isCornerEye = isFinderPattern(x, y, matrixWidth, matrixHeight);
                        g2.setColor(isCornerEye ? eyeColor : fgColor);

                        double px = x * cellWidth;
                        double py = y * cellHeight;
                        double pw = cellWidth;
                        double ph = cellHeight;

                        if ("dots".equalsIgnoreCase(dotStyle)) {
                            // Daire / Nokta deseni
                            double padding = cellWidth * 0.1;
                            Shape circle = new Ellipse2D.Double(px + padding, py + padding, pw - 2 * padding, ph - 2 * padding);
                            g2.fill(circle);
                        } else if ("rounded".equalsIgnoreCase(dotStyle)) {
                            // Yuvarlatılmış köşeli kareler
                            double arc = cellWidth * 0.6;
                            Shape roundRect = new RoundRectangle2D.Double(px, py, pw, ph, arc, arc);
                            g2.fill(roundRect);
                        } else {
                            // Klasik Kare
                            g2.fillRect((int) Math.round(px), (int) Math.round(py), (int) Math.ceil(pw), (int) Math.ceil(ph));
                        }
                    }
                }
            }

            // Merkez logo yerleştirme (Varsa)
            if (logoBase64 != null && !logoBase64.isBlank()) {
                try {
                    String cleanBase64 = logoBase64.contains(",") ? logoBase64.split(",")[1] : logoBase64;
                    byte[] logoBytes = Base64.getDecoder().decode(cleanBase64);
                    BufferedImage logoImage = ImageIO.read(new ByteArrayInputStream(logoBytes));

                    if (logoImage != null) {
                        int logoSize = (int) (size * 0.22); // QR boyutunun %22'si
                        int logoX = (size - logoSize) / 2;
                        int logoY = (size - logoSize) / 2;
                        int badgePadding = (int) (logoSize * 0.15);

                        // Logo arkasına beyaz koruma rozeti (Badge)
                        g2.setColor(Color.WHITE);
                        int badgeSize = logoSize + badgePadding * 2;
                        int badgeX = (size - badgeSize) / 2;
                        int badgeY = (size - badgeSize) / 2;
                        int badgeArc = (int) (badgeSize * 0.35);

                        g2.fill(new RoundRectangle2D.Double(badgeX, badgeY, badgeSize, badgeSize, badgeArc, badgeArc));
                        g2.setColor(new Color(228, 228, 231)); // zinc-200 border
                        g2.setStroke(new BasicStroke(Math.max(2, size / 200)));
                        g2.draw(new RoundRectangle2D.Double(badgeX, badgeY, badgeSize, badgeSize, badgeArc, badgeArc));

                        // Logoyu çiz
                        g2.drawImage(logoImage, logoX, logoY, logoSize, logoSize, null);
                    }
                } catch (Exception e) {
                    log.warn("QR logo eklenirken hata oluştu: {}", e.getMessage());
                }
            }

            g2.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Dinamik QR Kod üretilirken hata: ", e);
            throw new RuntimeException("Dinamik QR Kod üretilemedi: " + e.getMessage(), e);
        }
    }

    public String generateCustomQrCodeSvg(
            String content,
            int size,
            String fgColorHex,
            String bgColorHex,
            String eyeColorHex,
            String dotStyle) {
        try {
            if (size < 100) size = 400;

            String fgColor = formatHexColor(fgColorHex, "#000000");
            String bgColor = formatHexColor(bgColorHex, "#ffffff");
            String eyeColor = formatHexColor(eyeColorHex, fgColor);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            int matrixWidth = bitMatrix.getWidth();
            int matrixHeight = bitMatrix.getHeight();

            double cellWidth = (double) size / matrixWidth;
            double cellHeight = (double) size / matrixHeight;

            StringBuilder svg = new StringBuilder();
            svg.append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 %d %d\" width=\"%d\" height=\"%d\">\n", size, size, size, size));
            svg.append(String.format("  <rect width=\"100%%\" height=\"100%%\" fill=\"%s\"/>\n", bgColor));

            for (int x = 0; x < matrixWidth; x++) {
                for (int y = 0; y < matrixHeight; y++) {
                    if (bitMatrix.get(x, y)) {
                        boolean isCornerEye = isFinderPattern(x, y, matrixWidth, matrixHeight);
                        String fill = isCornerEye ? eyeColor : fgColor;
                        double px = x * cellWidth;
                        double py = y * cellHeight;

                        if ("dots".equalsIgnoreCase(dotStyle)) {
                            double radius = (cellWidth * 0.8) / 2.0;
                            double cx = px + cellWidth / 2.0;
                            double cy = py + cellHeight / 2.0;
                            svg.append(String.format("  <circle cx=\"%.2f\" cy=\"%.2f\" r=\"%.2f\" fill=\"%s\"/>\n", cx, cy, radius, fill));
                        } else if ("rounded".equalsIgnoreCase(dotStyle)) {
                            double rx = cellWidth * 0.3;
                            svg.append(String.format("  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" rx=\"%.2f\" fill=\"%s\"/>\n", px, py, cellWidth, cellHeight, rx, fill));
                        } else {
                            svg.append(String.format("  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\"/>\n", px, py, cellWidth, cellHeight, fill));
                        }
                    }
                }
            }

            svg.append("</svg>");
            return svg.toString();
        } catch (Exception e) {
            log.error("SVG QR Kod üretilirken hata: ", e);
            throw new RuntimeException("SVG QR Kod üretilemedi: " + e.getMessage(), e);
        }
    }

    private boolean isFinderPattern(int x, int y, int width, int height) {
        // Sol Üst Göz (7x7)
        if (x < 7 && y < 7) return true;
        // Sağ Üst Göz (7x7)
        if (x >= width - 7 && y < 7) return true;
        // Sol Alt Göz (7x7)
        if (x < 7 && y >= height - 7) return true;
        return false;
    }

    private Color parseColor(String hex, Color defaultColor) {
        if (hex == null || hex.isBlank()) return defaultColor;
        try {
            String clean = hex.trim();
            if (!clean.startsWith("#")) clean = "#" + clean;
            if (clean.length() == 4) { // #RGB -> #RRGGBB
                clean = "#" + clean.charAt(1) + clean.charAt(1) + clean.charAt(2) + clean.charAt(2) + clean.charAt(3) + clean.charAt(3);
            }
            return Color.decode(clean);
        } catch (Exception e) {
            return defaultColor;
        }
    }

    private String formatHexColor(String hex, String defaultHex) {
        if (hex == null || hex.isBlank()) return defaultHex;
        String clean = hex.trim();
        if (!clean.startsWith("#")) clean = "#" + clean;
        if (clean.matches("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")) {
            return clean;
        }
        return defaultHex;
    }
}
