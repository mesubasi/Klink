package com.urlshortener.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class UrlVariantRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Varyant etiketi boş olamaz.")
    private String label;

    @NotBlank(message = "Hedef URL adresi boş olamaz.")
    private String targetUrl;

    @NotNull(message = "Ağırlık yüzdesi belirtilmelidir.")
    @Min(value = 1, message = "Ağırlık en az %1 olmalıdır.")
    @Max(value = 100, message = "Ağırlık en fazla %100 olabilir.")
    private Integer weightPercent;

    public UrlVariantRequest() {}

    public UrlVariantRequest(String label, String targetUrl, Integer weightPercent) {
        this.label = label;
        this.targetUrl = targetUrl;
        this.weightPercent = weightPercent;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public Integer getWeightPercent() { return weightPercent; }
    public void setWeightPercent(Integer weightPercent) { this.weightPercent = weightPercent; }
}
