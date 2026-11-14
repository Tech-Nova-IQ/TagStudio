package com.technova.tagstudio.dto;

import java.util.List;

public class TagStudioResponseDTO {
    private String channelName;
    private String videoTopic;
    private String coreLabel;
    private int totalCategories;
    private int totalItems;
    private List<CategoryDataDTO> categories;
    private MetadataDTO metadata;
    private PlaylistMetadataDTO playlistMetadata;

    public TagStudioResponseDTO() {}

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

    public String getCoreLabel() {
        return coreLabel;
    }

    public void setCoreLabel(String coreLabel) {
        this.coreLabel = coreLabel;
    }

    public int getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(int totalCategories) {
        this.totalCategories = totalCategories;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public List<CategoryDataDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDataDTO> categories) {
        this.categories = categories;
    }

    public MetadataDTO getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataDTO metadata) {
        this.metadata = metadata;
    }

    public PlaylistMetadataDTO getPlaylistMetadata() {
        return playlistMetadata;
    }

    public void setPlaylistMetadata(PlaylistMetadataDTO playlistMetadata) {
        this.playlistMetadata = playlistMetadata;
    }
}
