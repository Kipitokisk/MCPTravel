package com.example.MCPTravel.controller;

import com.example.MCPTravel.dto.auth.AuthResponse;
import com.example.MCPTravel.dto.auth.LoginRequest;
import com.example.MCPTravel.dto.auth.RegisterRequest;
import com.example.MCPTravel.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Register a new user",
        description = "Create a new user account. Roles: USER, BUSINESS_OWNER, ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Registration successful, returns JWT token")
    @ApiResponse(responseCode = "400", description = "Validation error or user already exists")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
        summary = "Login",
        description = "Authenticate with username and password to receive a JWT token"
    )
    @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
