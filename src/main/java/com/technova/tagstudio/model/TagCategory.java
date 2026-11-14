package com.technova.tagstudio.model;

public enum TagCategory {
    CHANNEL("channel", "Channel", "#db2777"),
    TOPIC("topic", "Topic", "#0891b2"),
    CORE("core", "Core", "#d97706"),
    DATABASE("database", "Database", "#7c3aed"),
    DEVOPS("devops", "DevOps", "#dc2626"),
    MONITORING("monitoring", "Monitoring", "#059669"),
    PROGRAMMING("programming", "Programming", "#2563eb"),
    SHORTS("shorts", "Shorts", "#c026d3"),
    LOWCOMP("lowcomp", "Low-competition", "#a16207"),
    HIDDEN("hidden", "Hidden", "#4f46e5"),
    SEO("seo", "SEO", "#0d9488");

    private final String key;
    private final String defaultLabel;
    private final String color;

    TagCategory(String key, String defaultLabel, String color) {
        this.key = key;
        this.defaultLabel = defaultLabel;
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }

    public String getColor() {
        return color;
    }

    public static TagCategory fromKey(String key) {
        for (TagCategory cat : values()) {
            if (cat.key.equalsIgnoreCase(key)) {
                return cat;
            }
        }
        return null;
    }
}
