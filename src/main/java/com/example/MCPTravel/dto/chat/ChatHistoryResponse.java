package com.example.MCPTravel.dto.chat;

import com.example.MCPTravel.entity.ChatMessage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatHistoryResponse {
    private Long id;
    private String sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    public static ChatHistoryResponse fromEntity(ChatMessage message) {
        return ChatHistoryResponse.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
