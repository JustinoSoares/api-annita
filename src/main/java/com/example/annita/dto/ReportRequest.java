package com.example.annita.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {

    @NotBlank(message = "Motivo é obrigatório")
    private String reason;
}
