package com.technova.tagstudio.service;

import com.technova.tagstudio.dto.YoutubeActivityDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class YoutubeActivityService {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeActivityService.class);

    public YoutubeActivityDTO getActivityForQuery(String query) {
        logger.info("Fetching YouTube activity estimate for query: '{}'", query);
        if (query == null || query.trim().isEmpty()) {
            return new YoutubeActivityDTO(query, "Query cannot be empty");
        }

        // Simulate calculation based on phrase characteristics for realistic metrics
        String cleaned = query.trim().toLowerCase();
        int hash = Math.abs(cleaned.hashCode());
        
        long baseViews = 15000L + (hash % 185000L);
        if (cleaned.contains("shorts") || cleaned.contains("viral")) {
            baseViews *= 3;
        } else if (cleaned.contains("tutorial") || cleaned.contains("guide")) {
            baseViews *= 1.5;
        }

        long avgViews = Math.max(1200L, baseViews);

        return new YoutubeActivityDTO(query.trim(), avgViews, "active");
    }
}
