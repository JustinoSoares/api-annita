package com.example.annita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Nome de usuário é obrigatório")
    @Size(min = 3, max = 50, message = "Nome de usuário deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser um endereço válido")
    @Size(max = 100, message = "Email não pode exceder 100 caracteres")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;

    private boolean receiveNotifications = true;
}
