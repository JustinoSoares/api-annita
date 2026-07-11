package com.example.annita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Nome de usuário ou email é obrigatório")
    @Size(max = 50, message = "Nome de usuário ou email não pode exceder 50 caracteres")
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    @Size(max = 100, message = "Senha não pode exceder 100 caracteres")
    private String password;
}
