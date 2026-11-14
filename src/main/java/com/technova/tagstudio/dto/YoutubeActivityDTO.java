package com.technova.tagstudio.dto;

public class YoutubeActivityDTO {
    private String query;
    private long avgViews;
    private String status;
    private String error;

    public YoutubeActivityDTO() {}

    public YoutubeActivityDTO(String query, long avgViews, String status) {
        this.query = query;
        this.avgViews = avgViews;
        this.status = status;
    }

    public YoutubeActivityDTO(String query, String error) {
        this.query = query;
        this.error = error;
        this.status = "error";
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public long getAvgViews() {
        return avgViews;
    }

    public void setAvgViews(long avgViews) {
        this.avgViews = avgViews;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
