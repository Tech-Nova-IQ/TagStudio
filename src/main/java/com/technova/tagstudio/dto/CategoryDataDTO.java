package com.technova.tagstudio.dto;

import java.util.List;

public class CategoryDataDTO {
    private String key;
    private String label;
    private String color;
    private List<String> rawPhrases;
    private List<String> hashtags;
    private List<String> keywords;

    public CategoryDataDTO() {}

    public CategoryDataDTO(String key, String label, String color, List<String> rawPhrases, List<String> hashtags, List<String> keywords) {
        this.key = key;
        this.label = label;
        this.color = color;
        this.rawPhrases = rawPhrases;
        this.hashtags = hashtags;
        this.keywords = keywords;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getRawPhrases() {
        return rawPhrases;
    }

    public void setRawPhrases(List<String> rawPhrases) {
        this.rawPhrases = rawPhrases;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
