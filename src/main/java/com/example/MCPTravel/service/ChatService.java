package com.example.MCPTravel.service;

import com.example.MCPTravel.dto.chat.ChatHistoryResponse;
import com.example.MCPTravel.dto.chat.ChatRequest;
import com.example.MCPTravel.dto.chat.ChatResponse;
import com.example.MCPTravel.entity.ChatMessage;
import com.example.MCPTravel.entity.User;
import com.example.MCPTravel.repository.ChatMessageRepository;
import com.example.MCPTravel.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {

    @Value("${anthropic.api.key:}")
    private String apiKey;

    @Value("${anthropic.model:claude-haiku-4-5-20251001}")
    private String model;

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebClient webClient;

    public ChatService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    // Store conversation history per session
    private final Map<String, List<Map<String, Object>>> conversationHistory = new ConcurrentHashMap<>();

    // Store context per session
    private final Map<String, Map<String, Object>> sessionContext = new ConcurrentHashMap<>();

    // MCP tools cache
    private List<Map<String, Object>> claudeTools = new ArrayList<>();
    private Map<String, Map<String, Object>> toolsData = new HashMap<>();
    private boolean toolsLoaded = false;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private synchronized void ensureToolsLoaded() {
        if (!toolsLoaded) {
            loadMcpTools();
            toolsLoaded = true;
        }
    }

    private void loadMcpTools() {
        try {
            log.info("Loading MCP tools...");
            WebClient localClient = WebClient.create("http://127.0.0.1:8080");
            String response = localClient.get()
                    .uri("/api/discovery/tools")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode tools = root.get("tools");

                if (tools != null && tools.isArray()) {
                    for (JsonNode tool : tools) {
                        String name = tool.get("name").asText();

                        // Store tool data for execution
                        Map<String, Object> toolData = new HashMap<>();
                        toolData.put("endpoint", tool.get("endpoint").asText());
                        toolData.put("method", tool.get("method").asText());

                        Map<String, JsonNode> params = new HashMap<>();
                        if (tool.has("parameters")) {
                            for (JsonNode param : tool.get("parameters")) {
                                params.put(param.get("name").asText(), param);
                            }
                        }
                        toolData.put("parameters", params);
                        toolsData.put(name, toolData);

                        // Convert to Claude tool format
                        Map<String, Object> claudeTool = new HashMap<>();
                        claudeTool.put("name", name);
                        claudeTool.put("description", tool.get("description").asText());

                        Map<String, Object> inputSchema = new HashMap<>();
                        inputSchema.put("type", "object");

                        Map<String, Object> properties = new HashMap<>();
                        List<String> required = new ArrayList<>();

                        if (tool.has("parameters")) {
                            for (JsonNode param : tool.get("parameters")) {
                                String paramName = param.get("name").asText();
                                String paramType = param.has("type") ? param.get("type").asText() : "string";

                                Map<String, Object> propDef = new HashMap<>();
                                propDef.put("type", mapType(paramType));
                                if (param.has("description")) {
                                    propDef.put("description", param.get("description").asText());
                                }
                                properties.put(paramName, propDef);

                                if (param.has("required") && param.get("required").asBoolean()) {
                                    required.add(paramName);
                                }
                            }
                        }

                        inputSchema.put("properties", properties);
                        inputSchema.put("required", required);
                        claudeTool.put("input_schema", inputSchema);

                        claudeTools.add(claudeTool);
                    }
                    log.info("Loaded {} MCP tools", claudeTools.size());
                }
            }
        } catch (Exception e) {
            log.error("Could not load MCP tools: {}", e.getMessage(), e);
        }

        if (claudeTools.isEmpty()) {
            log.warn("No MCP tools loaded - Claude will not be able to search businesses!");
        }
    }

    private String mapType(String type) {
        return switch (type) {
            case "number", "integer" -> "number";
            case "boolean" -> "boolean";
            default -> "string";
        };
    }

    public ChatResponse chat(ChatRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            return ChatResponse.builder()
                    .success(false)
                    .error("Anthropic API key not configured")
                    .build();
        }

        // Load tools on first request
        ensureToolsLoaded();

        log.info("Tools loaded: {}, Tool count: {}", toolsLoaded, claudeTools.size());

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        // Get or create conversation history
        List<Map<String, Object>> history = conversationHistory.computeIfAbsent(
                sessionId, k -> new ArrayList<>());

        // Get or create context
        Map<String, Object> context = sessionContext.computeIfAbsent(
                sessionId, k -> new HashMap<>());

        // Add location context if provided
        if (request.getLatitude() != null && request.getLongitude() != null) {
            context.put("latitude", request.getLatitude());
            context.put("longitude", request.getLongitude());
        }

        try {
            String response = processChat(request.getMessage(), history, context);

            // Persist messages if user is authenticated
            persistIfAuthenticated(sessionId, request.getMessage(), response);

            return ChatResponse.builder()
                    .response(response)
                    .sessionId(sessionId)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Chat error: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .sessionId(sessionId)
                    .build();
        }
    }

    private String processChat(String userMessage, List<Map<String, Object>> history,
                               Map<String, Object> context) throws JsonProcessingException {

        // Build system prompt
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("You are a helpful assistant for MCPTravel, a business discovery service.\n\n");
        systemPrompt.append("You help users find businesses like restaurants, cafes, bars, hotels in Moldova.\n\n");
        systemPrompt.append("STRICT FORMATTING RULES:\n");
        systemPrompt.append("- Respond in PLAIN TEXT only. NO markdown, NO bold, NO asterisks, NO bullet points.\n");
        systemPrompt.append("- Use simple line breaks and dashes for lists.\n");
        systemPrompt.append("- Never show function calls, tool names, or JSON in your response.\n");
        systemPrompt.append("- Never use ** or * or # or any formatting symbols.\n");
        systemPrompt.append("- Just write naturally as if speaking to someone.\n\n");
        systemPrompt.append("LOCATION RULES:\n");
        systemPrompt.append("- When the user says 'near me', 'nearby', or 'close to me', use the find_nearby tool with the user's coordinates provided in the context below.\n");
        systemPrompt.append("- If the user asks for nearby places but no coordinates are available in the context, ask them to share their location or specify a city/address.\n");
        systemPrompt.append("- Never guess or make up coordinates. Only use coordinates from the context or explicitly provided by the user.\n");
        systemPrompt.append("- This service covers businesses in Moldova. If the user asks about locations outside Moldova, let them know the service currently only covers Moldova.\n\n");
        systemPrompt.append("SAFETY RULES:\n");
        systemPrompt.append("- Only provide information that comes from tool results. Never invent or make up business names, addresses, menus, prices, or any other details.\n");
        systemPrompt.append("- If a tool returns no results, say so clearly and suggest the user try a different category or search term.\n");
        systemPrompt.append("- Never fabricate working hours, phone numbers, or any business information not present in the data.\n");
        systemPrompt.append("- If you are unsure about something, say you don't have that information rather than guessing.\n\n");
        systemPrompt.append("OTHER RULES:\n");
        systemPrompt.append("- Use the available tools to search for businesses.\n");
        systemPrompt.append("- Prices are in Moldovan Lei (MDL).\n");
        systemPrompt.append("- Be concise and friendly.\n");
        systemPrompt.append("- company_id must always be a NUMBER, never a name.\n");

        if (context.containsKey("last_company_id")) {
            systemPrompt.append("\nCONTEXT: Last discussed company was '")
                    .append(context.get("last_company_name"))
                    .append("' with id=")
                    .append(context.get("last_company_id"))
                    .append(".\n");
        }

        if (context.containsKey("latitude") && context.containsKey("longitude")) {
            systemPrompt.append("\nUser's location: lat=")
                    .append(context.get("latitude"))
                    .append(", lng=")
                    .append(context.get("longitude"))
                    .append(".\n");
        }

        // Add user message to history
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        history.add(userMsg);

        // Call Claude API
        JsonNode response = callClaude(systemPrompt.toString(), history);

        // Handle tool use loop
        int maxIterations = 5;
        int iteration = 0;

        while (response.has("stop_reason") &&
               "tool_use".equals(response.get("stop_reason").asText()) &&
               iteration < maxIterations) {

            iteration++;
            JsonNode content = response.get("content");

            // Add assistant response to history
            Map<String, Object> assistantMsg = new HashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", objectMapper.convertValue(content, List.class));
            history.add(assistantMsg);

            // Process tool calls
            List<Map<String, Object>> toolResults = new ArrayList<>();

            for (JsonNode block : content) {
                if ("tool_use".equals(block.get("type").asText())) {
                    String toolName = block.get("name").asText();
                    String toolUseId = block.get("id").asText();
                    JsonNode toolInput = block.get("input");

                    log.info("Calling tool: {}", toolName);

                    // Execute tool
                    Map<String, Object> result = executeTool(toolName, toolInput, context);

                    Map<String, Object> toolResult = new HashMap<>();
                    toolResult.put("type", "tool_result");
                    toolResult.put("tool_use_id", toolUseId);
                    toolResult.put("content", objectMapper.writeValueAsString(result));
                    toolResults.add(toolResult);
                }
            }

            // Add tool results to history
            Map<String, Object> toolResultMsg = new HashMap<>();
            toolResultMsg.put("role", "user");
            toolResultMsg.put("content", toolResults);
            history.add(toolResultMsg);

            // Get next response
            response = callClaude(systemPrompt.toString(), history);
        }

        // Extract final text response
        StringBuilder finalResponse = new StringBuilder();
        JsonNode content = response.get("content");
        if (content != null && content.isArray()) {
            for (JsonNode block : content) {
                if (block.has("text")) {
                    finalResponse.append(block.get("text").asText());
                }
            }
        }

        // Clean up any remaining formatting artifacts
        String cleanedResponse = cleanResponse(finalResponse.toString());

        // Add final response to history
        Map<String, Object> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("content", objectMapper.convertValue(content, List.class));
        history.add(assistantMsg);

        // Limit history size
        if (history.size() > 20) {
            history.subList(0, history.size() - 20).clear();
        }

        return cleanedResponse;
    }

    private String cleanResponse(String response) {
        if (response == null) return "";

        String cleaned = response;

        // Remove any function call blocks
        cleaned = cleaned.replaceAll("<function_calls>[\\s\\S]*?</function_calls>", "");
        cleaned = cleaned.replaceAll("<function_calls>[\\s\\S]*?</function_calls>", "");
        cleaned = cleaned.replaceAll("\\[\\{\"tool_name\"[^\\]]*\\}\\]", "");

        // Remove markdown formatting
        cleaned = cleaned.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");  // **bold**
        cleaned = cleaned.replaceAll("\\*([^*]+)\\*", "$1");        // *italic*
        cleaned = cleaned.replaceAll("^#+\\s*", "");                 // # headers
        cleaned = cleaned.replaceAll("\\n#+\\s*", "\n");             // # headers in text

        // Clean up extra whitespace
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");
        cleaned = cleaned.trim();

        return cleaned;
    }

    private JsonNode callClaude(String systemPrompt, List<Map<String, Object>> messages)
            throws JsonProcessingException {

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 1024);
        requestBody.put("system", systemPrompt);
        requestBody.set("messages", objectMapper.valueToTree(messages));
        requestBody.set("tools", objectMapper.valueToTree(claudeTools));

        String responseBody = webClient.post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return objectMapper.readTree(responseBody);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> executeTool(String toolName, JsonNode input, Map<String, Object> context) {
        try {
            Map<String, Object> toolDef = toolsData.get(toolName);
            if (toolDef == null) {
                return Map.of("error", "Unknown tool: " + toolName);
            }

            String endpoint = (String) toolDef.get("endpoint");

            // Replace path parameters
            Map<String, Object> queryParams = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = input.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                String placeholder = "{" + key + "}";
                if (endpoint.contains(placeholder)) {
                    endpoint = endpoint.replace(placeholder, value.asText());
                } else {
                    // Add as query parameter
                    if (value.isNumber()) {
                        queryParams.put(key, value.asDouble());
                    } else if (value.isBoolean()) {
                        queryParams.put(key, value.asBoolean());
                    } else {
                        queryParams.put(key, value.asText());
                    }
                }
            }

            // Build URL with query params
            StringBuilder url = new StringBuilder("http://127.0.0.1:8080" + endpoint);
            if (!queryParams.isEmpty()) {
                url.append("?");
                List<String> params = new ArrayList<>();
                for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                    params.add(entry.getKey() + "=" + entry.getValue());
                }
                url.append(String.join("&", params));
            }

            // Make request
            WebClient localClient = WebClient.create();
            String response = localClient.get()
                    .uri(url.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            // Store context from results
            if (result.containsKey("data") && result.get("data") instanceof List) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                if (!data.isEmpty()) {
                    Map<String, Object> first = data.get(0);
                    if (first.containsKey("id")) {
                        context.put("last_company_id", first.get("id"));
                        context.put("last_company_name", first.get("name"));
                    }

                    // Limit results to avoid token overflow
                    if (data.size() > 10) {
                        result.put("data", data.subList(0, 10));
                        result.put("truncated", true);
                    }
                }
            }

            return result;

        } catch (Exception e) {
            log.error("Tool execution error: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    public void clearSession(String sessionId) {
        conversationHistory.remove(sessionId);
        sessionContext.remove(sessionId);
    }

    private void persistIfAuthenticated(String sessionId, String userMessage, String assistantResponse) {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                return;
            }

            ChatMessage userMsg = ChatMessage.builder()
                    .user(user)
                    .sessionId(sessionId)
                    .role("user")
                    .content(userMessage)
                    .build();

            ChatMessage assistantMsg = ChatMessage.builder()
                    .user(user)
                    .sessionId(sessionId)
                    .role("assistant")
                    .content(assistantResponse)
                    .build();

            chatMessageRepository.saveAll(List.of(userMsg, assistantMsg));
        } catch (Exception e) {
            log.warn("Failed to persist chat messages: {}", e.getMessage());
        }
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<Map<String, Object>> getChatSessions() {
        User user = getAuthenticatedUser();
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }

        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagePerSession(user);
        return latestMessages.stream()
                .map(msg -> {
                    Map<String, Object> session = new HashMap<>();
                    session.put("sessionId", msg.getSessionId());
                    session.put("lastMessage", msg.getContent());
                    session.put("lastMessageAt", msg.getCreatedAt());
                    return session;
                })
                .collect(Collectors.toList());
    }

    public List<ChatHistoryResponse> getSessionHistory(String sessionId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }

        return chatMessageRepository.findByUserAndSessionIdOrderByCreatedAtAsc(user, sessionId)
                .stream()
                .map(ChatHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSessionHistory(String sessionId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }

        chatMessageRepository.deleteByUserAndSessionId(user, sessionId);
    }
}
