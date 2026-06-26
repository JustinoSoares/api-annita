package com.example.annita.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@RestController
@Tag(name = "Health", description = "Health check endpoint")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    @Operation(summary = "Health check", description = "Returns the server status and database connectivity.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Server is healthy",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(example = "{\"status\": \"UP\", \"database\": \"connected\"}")))
    })
    public ResponseEntity<Map<String, Object>> health() {
        String dbStatus;
        try (Connection conn = dataSource.getConnection()) {
            dbStatus = conn.isValid(2) ? "connected" : "disconnected";
        } catch (Exception e) {
            dbStatus = "error: " + e.getMessage();
        }

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "database", dbStatus
        ));
    }
}
