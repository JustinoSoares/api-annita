package com.example.annita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class NewsletterUpdateRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser um endereço válido")
    @Size(max = 100, message = "Email não pode exceder 100 caracteres")
    private String email;

    @NotBlank(message = "Código de verificação é obrigatório")
    @Size(min = 6, max = 6, message = "Código de verificação deve ter 6 dígitos")
    private String code;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    private List<UUID> categoryIds;
}
