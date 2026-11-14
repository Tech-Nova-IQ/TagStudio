package com.technova.tagstudio.controller;

import com.technova.tagstudio.dto.YoutubeActivityDTO;
import com.technova.tagstudio.service.YoutubeActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/youtube")
public class YoutubeApiController {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeApiController.class);

    private final YoutubeActivityService youtubeActivityService;

    public YoutubeApiController(YoutubeActivityService youtubeActivityService) {
        this.youtubeActivityService = youtubeActivityService;
    }

    @GetMapping("/activity")
    public ResponseEntity<YoutubeActivityDTO> getYoutubeActivity(@RequestParam("q") String query) {
        logger.info("REST request for YouTube activity query: {}", query);
        YoutubeActivityDTO activity = youtubeActivityService.getActivityForQuery(query);
        return ResponseEntity.ok(activity);
    }
}
