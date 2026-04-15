package com.example.MCPTravel.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    private String response;
    private String sessionId;
    private List<Map<String, Object>> toolsUsed;
    private boolean success;
    private String error;
}
