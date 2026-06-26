package com.example.annita.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Nome de usuário ou email é obrigatório")
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    private String password;
}
