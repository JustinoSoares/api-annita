package com.example.annita.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {

    @NotBlank(message = "O token do Google é obrigatório")
    @Size(max = 5000, message = "Token do Google inválido")
    @Schema(description = "Google ID token (credential) from Google Sign-In")
    private String idToken;
}
