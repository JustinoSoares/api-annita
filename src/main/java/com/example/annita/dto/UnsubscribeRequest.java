package com.example.annita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnsubscribeRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;
}
