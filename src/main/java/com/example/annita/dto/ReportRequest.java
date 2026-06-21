package com.example.annita.dto;

import com.example.annita.model.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {

    @NotNull(message = "Reason is required")
    private ReportReason reason;

    private String description;
}
