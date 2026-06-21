package com.example.annita.controller;

import com.example.annita.dto.LoginRequest;
import com.example.annita.dto.LoginResponse;
import com.example.annita.dto.RegisterRequest;
import com.example.annita.dto.UserResponse;
import com.example.annita.dto.VerifyEmailRequest;
import com.example.annita.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with the CONTRIBUTOR role.")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Returns a signed JWT bearer token and user details.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-verification-code")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "Send email verification code", description = "Sends a 6-digit code to the authenticated user's email.")
    public ResponseEntity<Void> sendVerificationCode(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        userService.sendVerificationCode(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "Verify email with code", description = "Verifies the authenticated user's email using the 6-digit code.")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request, Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        userService.verifyEmail(userId, request.getCode());
        return ResponseEntity.ok().build();
    }
}
