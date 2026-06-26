package com.example.annita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {

    @NotBlank(message = "Código de verificação é obrigatório")
    @Size(min = 6, max = 6, message = "Código de verificação deve ter 6 dígitos")
    private String code;
}
