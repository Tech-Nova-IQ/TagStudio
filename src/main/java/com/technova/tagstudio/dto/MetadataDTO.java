package com.technova.tagstudio.dto;

import java.util.List;
import java.util.Map;

public class MetadataDTO {
    private List<String> titleOptions;
    private String descriptionTemplate;
    private String ctaText;
    private Map<String, String> categoryTagsFields; // category key -> comma-separated tags (<500 chars)

    public MetadataDTO() {}

    public MetadataDTO(List<String> titleOptions, String descriptionTemplate, Map<String, String> categoryTagsFields) {
        this.titleOptions = titleOptions;
        this.descriptionTemplate = descriptionTemplate;
        this.categoryTagsFields = categoryTagsFields;
    }

    public MetadataDTO(List<String> titleOptions, String descriptionTemplate, String ctaText, Map<String, String> categoryTagsFields) {
        this.titleOptions = titleOptions;
        this.descriptionTemplate = descriptionTemplate;
        this.ctaText = ctaText;
        this.categoryTagsFields = categoryTagsFields;
    }

    public List<String> getTitleOptions() {
        return titleOptions;
    }

    public void setTitleOptions(List<String> titleOptions) {
        this.titleOptions = titleOptions;
    }

    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }

    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public Map<String, String> getCategoryTagsFields() {
        return categoryTagsFields;
    }

    public void setCategoryTagsFields(Map<String, String> categoryTagsFields) {
        this.categoryTagsFields = categoryTagsFields;
    }
}
