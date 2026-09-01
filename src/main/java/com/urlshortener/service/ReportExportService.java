package com.urlshortener.service;

import com.urlshortener.model.ClickAnalytics;
import com.urlshortener.model.UrlMapping;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportExportService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateCsvReport(UrlMapping mapping, List<ClickAnalytics> clicks) {
        StringBuilder csv = new StringBuilder();
        // CSV Header
        csv.append("Tıklama ID,Kısa Kod,Tıklama Tarihi,IP Adresi,Ülke,Şehir,Cihaz (User Agent),Referrer Kaynağı\n");

        for (ClickAnalytics click : clicks) {
            String dateStr = click.getClickedAt() != null
                    ? Instant.ofEpochMilli(click.getClickedAt()).atZone(ZoneId.systemDefault()).format(FORMATTER)
                    : "";

            csv.append(click.getId() != null ? click.getId().toString() : "").append(",")
               .append(escapeCsv(click.getShortCode())).append(",")
               .append(dateStr).append(",")
               .append(escapeCsv(click.getIpAddress())).append(",")
               .append(escapeCsv(click.getCountry() != null ? click.getCountry() : "Türkiye")).append(",")
               .append(escapeCsv(click.getCity() != null ? click.getCity() : "İstanbul")).append(",")
               .append(escapeCsv(click.getUserAgent())).append(",")
               .append(escapeCsv(click.getReferrer())).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generatePdfReport(UrlMapping mapping, List<ClickAnalytics> clicks) {
        StringBuilder pdfText = new StringBuilder();
        pdfText.append("=================================================================\n");
        pdfText.append("            KLINK ENTERPRISE TELEMETRY & ANALYTICS REPORT        \n");
        pdfText.append("=================================================================\n\n");
        pdfText.append("Kısa Kod (Short Code) : /").append(mapping.getShortCode()).append("\n");
        pdfText.append("Hedef Web Adresi      : ").append(mapping.getOriginalUrl()).append("\n");
        pdfText.append("Toplam Tıklama        : ").append(mapping.getClickCount()).append("\n");

        String createdDateStr = mapping.getCreatedAt() != null
                ? Instant.ofEpochMilli(mapping.getCreatedAt()).atZone(ZoneId.systemDefault()).format(FORMATTER)
                : "N/A";
        pdfText.append("Oluşturulma Tarihi    : ").append(createdDateStr).append("\n");
        pdfText.append("-----------------------------------------------------------------\n\n");
        pdfText.append("DETAYLI TIKLAMA KAYITLARI (SON 50 İŞLEM):\n\n");

        for (int i = 0; i < clicks.size(); i++) {
            ClickAnalytics click = clicks.get(i);
            String dateStr = click.getClickedAt() != null
                    ? Instant.ofEpochMilli(click.getClickedAt()).atZone(ZoneId.systemDefault()).format(FORMATTER)
                    : "N/A";

            pdfText.append(String.format("#%02d | %s | IP: %s | %s/%s | %s\n",
                    i + 1,
                    dateStr,
                    click.getIpAddress() != null ? click.getIpAddress() : "127.0.0.1",
                    click.getCountry() != null ? click.getCountry() : "Türkiye",
                    click.getCity() != null ? click.getCity() : "İstanbul",
                    click.getReferrer() != null ? click.getReferrer() : "Doğrudan"
            ));
        }

        pdfText.append("\n=================================================================\n");
        pdfText.append("Rapor Oluşturulma Zamanı: ").append(Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).format(FORMATTER)).append("\n");

        return pdfText.toString().getBytes(StandardCharsets.UTF_8);
    }

    public String generateEmailReportHtml(UrlMapping mapping, List<ClickAnalytics> clicks, int[][] heatmap) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; background-color: #f9f9fb; padding: 24px;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #e4e4e7; padding: 28px;'>");
        html.append("<div style='display: flex; align-items: center; gap: 8px; margin-bottom: 20px;'>");
        html.append("<h2 style='color: #09090b; margin: 0; font-size: 20px;'>⚡ Klink Haftalık Analitik Raporu</h2>");
        html.append("</div>");
        html.append("<p style='color: #71717a; font-size: 13px; margin: 0 0 20px;'>Aşağıda <strong>/").append(mapping.getShortCode()).append("</strong> linkinize ait haftalık performans ve tıklama özeti yer almaktadır.</p>");
        html.append("<div style='background: #f4f4f5; border-radius: 12px; padding: 16px; margin-bottom: 20px;'>");
        html.append("<div style='font-size: 12px; color: #71717a;'>Toplam Tıklama</div>");
        html.append("<div style='font-size: 28px; font-weight: bold; color: #09090b;'>").append(mapping.getClickCount()).append("</div>");
        html.append("<div style='font-size: 12px; color: #52525b; margin-top: 4px;'>Hedef: ").append(mapping.getOriginalUrl()).append("</div>");
        html.append("</div>");
        html.append("<p style='color: #a1a1aa; font-size: 11px; margin-top: 24px; text-align: center;'>Klink Telemetry Engine &copy; 2026</p>");
        html.append("</div></body></html>");
        return html.toString();
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        String escaped = data.replaceAll("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
