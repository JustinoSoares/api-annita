package com.example.annita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(max = 100, message = "Nome da categoria deve ter no máximo 100 caracteres")
    private String name;

    @NotBlank(message = "Nome do grupo é obrigatório")
    @Size(max = 100, message = "Nome do grupo deve ter no máximo 100 caracteres")
    private String groupName;
}
