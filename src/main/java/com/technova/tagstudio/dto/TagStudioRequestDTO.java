package com.technova.tagstudio.dto;

import java.util.List;

public class TagStudioRequestDTO {
    private String channelName;
    private String videoTopic;
    private List<String> selectedCategories;
    private String prompt;

    public TagStudioRequestDTO() {}

    public TagStudioRequestDTO(String channelName, String videoTopic, List<String> selectedCategories) {
        this.channelName = channelName;
        this.videoTopic = videoTopic;
        this.selectedCategories = selectedCategories;
    }

    public TagStudioRequestDTO(String channelName, String videoTopic, List<String> selectedCategories, String prompt) {
        this.channelName = channelName;
        this.videoTopic = videoTopic;
        this.selectedCategories = selectedCategories;
        this.prompt = prompt;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getVideoTopic() {
        return videoTopic;
    }

    public void setVideoTopic(String videoTopic) {
        this.videoTopic = videoTopic;
    }

    public List<String> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(List<String> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
