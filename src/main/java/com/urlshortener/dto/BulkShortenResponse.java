package com.urlshortener.dto;

import java.util.List;

public class BulkShortenResponse {

    private int totalCount;
    private int successCount;
    private List<ShortenResponse> shortenedUrls;

    public BulkShortenResponse() {}

    public BulkShortenResponse(int totalCount, int successCount, List<ShortenResponse> shortenedUrls) {
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.shortenedUrls = shortenedUrls;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int totalCount;
        private int successCount;
        private List<ShortenResponse> shortenedUrls;

        public Builder totalCount(int totalCount) { this.totalCount = totalCount; return this; }
        public Builder successCount(int successCount) { this.successCount = successCount; return this; }
        public Builder shortenedUrls(List<ShortenResponse> shortenedUrls) { this.shortenedUrls = shortenedUrls; return this; }

        public BulkShortenResponse build() {
            return new BulkShortenResponse(totalCount, successCount, shortenedUrls);
        }
    }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public List<ShortenResponse> getShortenedUrls() { return shortenedUrls; }
    public void setShortenedUrls(List<ShortenResponse> shortenedUrls) { this.shortenedUrls = shortenedUrls; }
}
