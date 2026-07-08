package com.example.annita.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {

    @NotBlank(message = "O token do Google é obrigatório")
    @Schema(description = "Google ID token (credential) from Google Sign-In")
    private String idToken;
}
