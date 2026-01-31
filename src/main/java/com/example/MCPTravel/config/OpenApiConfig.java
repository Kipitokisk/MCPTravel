package com.example.MCPTravel.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mcpTravelOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MCPTravel API")
                        .description("MCP-based platform for business discovery with real-time data. " +
                                "Allows AI agents to discover restaurants, cafes, and other businesses " +
                                "using natural language queries.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Revenco Victor")
                                .email("victor.revenco@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token authentication. Get token from /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
