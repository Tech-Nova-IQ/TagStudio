package com.technova.tagstudio.model;

public class CategoryInfo {
    private String key;
    private String label;
    private String color;
    private int count;

    public CategoryInfo() {}

    public CategoryInfo(String key, String label, String color, int count) {
        this.key = key;
        this.label = label;
        this.color = color;
        this.count = count;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
