package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class CustomQrRequest {

    @Schema(description = "QR Koda dönüştürülecek içerik (Link veya Metin)", example = "https://klink.to/summer-sale")
    private String content;

    @Schema(description = "Genişlik (piksel)", example = "512")
    private int width = 512;

    @Schema(description = "Yükseklik (piksel)", example = "512")
    private int height = 512;

    @Schema(description = "Ön plan / Desen rengi (Hex formatında)", example = "#09090b")
    private String fgColor = "#000000";

    @Schema(description = "Arka plan rengi (Hex formatında)", example = "#ffffff")
    private String bgColor = "#ffffff";

    @Schema(description = "Köşe göz rengi (Hex formatında)", example = "#10b981")
    private String eyeColor;

    @Schema(description = "Nokta / Desen stili (square, dots, rounded)", example = "dots")
    private String dotStyle = "square";

    @Schema(description = "Merkez Logo (Base64 veri)", example = "data:image/png;base64,iVBORw0KGgo...")
    private String logoBase64;

    @Schema(description = "Çıktı formatı (png, svg)", example = "png")
    private String format = "png";

    public CustomQrRequest() {}

    public CustomQrRequest(String content, int width, int height, String fgColor, String bgColor, String eyeColor, String dotStyle, String logoBase64, String format) {
        this.content = content;
        this.width = width;
        this.height = height;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.eyeColor = eyeColor;
        this.dotStyle = dotStyle;
        this.logoBase64 = logoBase64;
        this.format = format;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public String getFgColor() { return fgColor; }
    public void setFgColor(String fgColor) { this.fgColor = fgColor; }
    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    public String getEyeColor() { return eyeColor; }
    public void setEyeColor(String eyeColor) { this.eyeColor = eyeColor; }
    public String getDotStyle() { return dotStyle; }
    public void setDotStyle(String dotStyle) { this.dotStyle = dotStyle; }
    public String getLogoBase64() { return logoBase64; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}
