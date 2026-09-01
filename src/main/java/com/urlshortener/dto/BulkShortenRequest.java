package com.urlshortener.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BulkShortenRequest {

    @NotEmpty(message = "{validation.bulk.empty}")
    @Size(max = 50, message = "{validation.bulk.max_size}")
    @Valid
    private List<ShortenRequest> urls;

    public BulkShortenRequest() {}

    public BulkShortenRequest(List<ShortenRequest> urls) {
        this.urls = urls;
    }

    public List<ShortenRequest> getUrls() { return urls; }
    public void setUrls(List<ShortenRequest> urls) { this.urls = urls; }
}
