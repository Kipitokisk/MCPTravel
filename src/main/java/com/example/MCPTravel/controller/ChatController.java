package com.example.MCPTravel.controller;

import com.example.MCPTravel.dto.chat.ChatHistoryResponse;
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

import java.util.List;
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
        description = "Clears in-memory conversation history for a specific session"
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

    @Operation(
        summary = "Get chat sessions",
        description = "Returns all chat sessions for the authenticated user with the last message from each session."
    )
    @ApiResponse(responseCode = "200", description = "List of chat sessions")
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getChatSessions() {
        List<Map<String, Object>> sessions = chatService.getChatSessions();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "sessions", sessions,
            "count", sessions.size()
        ));
    }

    @Operation(
        summary = "Get session history",
        description = "Returns all messages for a specific chat session belonging to the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "List of messages in the session")
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionHistory(@PathVariable String sessionId) {
        List<ChatHistoryResponse> messages = chatService.getSessionHistory(sessionId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "sessionId", sessionId,
            "messages", messages,
            "count", messages.size()
        ));
    }

    @Operation(
        summary = "Delete session history",
        description = "Deletes all persisted messages for a specific chat session belonging to the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Session history deleted")
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSessionHistory(@PathVariable String sessionId) {
        chatService.deleteSessionHistory(sessionId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Session history deleted"
        ));
    }
}
