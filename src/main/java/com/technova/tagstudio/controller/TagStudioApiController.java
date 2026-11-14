package com.technova.tagstudio.controller;

import com.technova.tagstudio.dto.TagStudioRequestDTO;
import com.technova.tagstudio.dto.TagStudioResponseDTO;
import com.technova.tagstudio.model.TagCategory;
import com.technova.tagstudio.service.TagGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
public class TagStudioApiController {

    private static final Logger logger = LoggerFactory.getLogger(TagStudioApiController.class);

    private final TagGeneratorService tagGeneratorService;

    public TagStudioApiController(TagGeneratorService tagGeneratorService) {
        this.tagGeneratorService = tagGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TagStudioResponseDTO> generateTags(@RequestBody TagStudioRequestDTO request) {
        logger.info("REST request to generate tags for topic: {}", request.getVideoTopic());
        TagStudioResponseDTO response = tagGeneratorService.generateStudioData(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<TagCategory>> getCategories() {
        return ResponseEntity.ok(Arrays.stream(TagCategory.values()).collect(Collectors.toList()));
    }
}
