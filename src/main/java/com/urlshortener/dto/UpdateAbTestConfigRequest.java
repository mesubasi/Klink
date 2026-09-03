package com.urlshortener.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class UpdateAbTestConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "A/B test aktiflik durumu belirtilmelidir.")
    private Boolean abTestingEnabled;

    @Valid
    private List<UrlVariantRequest> variants;

    public UpdateAbTestConfigRequest() {}

    public UpdateAbTestConfigRequest(Boolean abTestingEnabled, List<UrlVariantRequest> variants) {
        this.abTestingEnabled = abTestingEnabled;
        this.variants = variants;
    }

    public Boolean getAbTestingEnabled() { return abTestingEnabled; }
    public void setAbTestingEnabled(Boolean abTestingEnabled) { this.abTestingEnabled = abTestingEnabled; }

    public List<UrlVariantRequest> getVariants() { return variants; }
    public void setVariants(List<UrlVariantRequest> variants) { this.variants = variants; }
}
