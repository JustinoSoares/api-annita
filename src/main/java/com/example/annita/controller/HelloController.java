package com.example.annita.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    @Operation(summary = "Root endpoint", description = "Returns a simple greeting message.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Greeting message",
                     content = @Content(mediaType = "text/plain"))
    })
    public String hello() {
        return "Hello, World!";
    }
}
