package com.example.annita.controller;

import com.example.annita.dto.LoginRequest;
import com.example.annita.dto.LoginResponse;
import com.example.annita.dto.RegisterRequest;
import com.example.annita.dto.UsernameAvailabilityResponse;
import com.example.annita.dto.UserResponse;
import com.example.annita.dto.VerifyEmailRequest;
import com.example.annita.model.User;
import com.example.annita.service.TokenService;
import com.example.annita.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with the CONTRIBUTOR role.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email or username already in use")
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/check-username")
    @Operation(summary = "Check if username is already taken")
    public ResponseEntity<UsernameAvailabilityResponse> checkUsername(@RequestParam String username) {
        boolean taken = userService.isUsernameTaken(username);
        return ResponseEntity.ok(new UsernameAvailabilityResponse(!taken));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Returns a signed JWT bearer token and user details.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-verification-code")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Send email verification code", description = "Sends a 6-digit code to the authenticated user's email.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Code sent successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    public ResponseEntity<Void> sendVerificationCode(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        userService.sendVerificationCode(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    //@PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Verify email with code", description = "Verifies the authenticated user's email using the 6-digit code and returns a new JWT token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Email verified successfully; returns a new JWT token",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid code, expired code, or email already verified"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<LoginResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        User user = userService.verifyEmail(userId, request.getCode());
        String token = tokenService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
