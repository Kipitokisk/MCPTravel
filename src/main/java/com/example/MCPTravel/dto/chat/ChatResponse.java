package com.example.MCPTravel.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    private String response;
    private String sessionId;
    private boolean success;
    private String error;

    // Current location from conversation context
    private String locationName;
    private Double locationLatitude;
    private Double locationLongitude;
}
