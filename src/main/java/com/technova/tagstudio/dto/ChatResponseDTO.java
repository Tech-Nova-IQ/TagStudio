package com.technova.tagstudio.dto;

public class ChatResponseDTO {
    private String reply;
    private String error;

    public ChatResponseDTO() {}

    public ChatResponseDTO(String reply) {
        this.reply = reply;
    }

    public static ChatResponseDTO error(String errorMessage) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setError(errorMessage);
        return dto;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
