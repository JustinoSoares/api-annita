package com.example.annita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnsubscribeConfirmRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de email inválido")
    @Size(max = 100, message = "Email não pode exceder 100 caracteres")
    private String email;

    @NotBlank(message = "Código de verificação é obrigatório")
    @Size(min = 6, max = 6, message = "Código de verificação deve ter 6 dígitos")
    private String code;
}
