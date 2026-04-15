package com.example.MCPTravel.controller;

import com.example.MCPTravel.dto.chat.ChatRequest;
import com.example.MCPTravel.dto.chat.ChatResponse;
import com.example.MCPTravel.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI Chat interface powered by Claude")
public class ChatController {

    private final ChatService chatService;

    @Operation(
        summary = "Send a chat message",
        description = "Send a message to the AI assistant. Optionally include sessionId for conversation continuity and coordinates for location-based queries."
    )
    @ApiResponse(responseCode = "200", description = "Chat response")
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Clear chat session",
        description = "Clears conversation history for a specific session"
    )
    @ApiResponse(responseCode = "200", description = "Session cleared")
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> clearSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Session cleared"
        ));
    }
}
