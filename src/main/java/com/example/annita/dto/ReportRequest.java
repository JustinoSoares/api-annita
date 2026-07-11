package com.example.annita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {

    @NotBlank(message = "Motivo é obrigatório")
    @Size(max = 1000, message = "Motivo deve ter no máximo 1000 caracteres")
    private String reason;
}
