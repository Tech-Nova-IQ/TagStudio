package com.technova.tagstudio.dto;

import java.util.List;

public class PlaylistMetadataDTO {
    private List<String> playlistNames;
    private String playlistDescription;
    private String ctaText;
    private List<String> highReachHashtags;
    private List<String> seoKeywordsList;
    private String practicalTagsField;
    private String thumbnailConcept;
    private String thumbnailAiPrompt;
    private List<String> lowCompHashtags;
    private List<String> lowCompKeywords;
    private List<String> hiddenHashtags;
    private List<String> hiddenKeywords;

    public PlaylistMetadataDTO() {}

    public List<String> getPlaylistNames() {
        return playlistNames;
    }

    public void setPlaylistNames(List<String> playlistNames) {
        this.playlistNames = playlistNames;
    }

    public String getPlaylistDescription() {
        return playlistDescription;
    }

    public void setPlaylistDescription(String playlistDescription) {
        this.playlistDescription = playlistDescription;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public List<String> getHighReachHashtags() {
        return highReachHashtags;
    }

    public void setHighReachHashtags(List<String> highReachHashtags) {
        this.highReachHashtags = highReachHashtags;
    }

    public List<String> getSeoKeywordsList() {
        return seoKeywordsList;
    }

    public void setSeoKeywordsList(List<String> seoKeywordsList) {
        this.seoKeywordsList = seoKeywordsList;
    }

    public String getPracticalTagsField() {
        return practicalTagsField;
    }

    public void setPracticalTagsField(String practicalTagsField) {
        this.practicalTagsField = practicalTagsField;
    }

    public String getThumbnailConcept() {
        return thumbnailConcept;
    }

    public void setThumbnailConcept(String thumbnailConcept) {
        this.thumbnailConcept = thumbnailConcept;
    }

    public String getThumbnailAiPrompt() {
        return thumbnailAiPrompt;
    }

    public void setThumbnailAiPrompt(String thumbnailAiPrompt) {
        this.thumbnailAiPrompt = thumbnailAiPrompt;
    }

    public List<String> getLowCompHashtags() {
        return lowCompHashtags;
    }

    public void setLowCompHashtags(List<String> lowCompHashtags) {
        this.lowCompHashtags = lowCompHashtags;
    }

    public List<String> getLowCompKeywords() {
        return lowCompKeywords;
    }

    public void setLowCompKeywords(List<String> lowCompKeywords) {
        this.lowCompKeywords = lowCompKeywords;
    }

    public List<String> getHiddenHashtags() {
        return hiddenHashtags;
    }

    public void setHiddenHashtags(List<String> hiddenHashtags) {
        this.hiddenHashtags = hiddenHashtags;
    }

    public List<String> getHiddenKeywords() {
        return hiddenKeywords;
    }

    public void setHiddenKeywords(List<String> hiddenKeywords) {
        this.hiddenKeywords = hiddenKeywords;
    }
}
