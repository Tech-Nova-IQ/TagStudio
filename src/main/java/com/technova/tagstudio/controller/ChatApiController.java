package com.technova.tagstudio.controller;

import com.technova.tagstudio.dto.ChatRequestDTO;
import com.technova.tagstudio.dto.ChatResponseDTO;
import com.technova.tagstudio.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private static final Logger logger = LoggerFactory.getLogger(ChatApiController.class);

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> handleChat(@RequestBody ChatRequestDTO request) {
        logger.info("REST chat request received");
        ChatResponseDTO response = chatService.processChat(request);
        return ResponseEntity.ok(response);
    }
}
