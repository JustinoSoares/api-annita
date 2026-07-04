package com.example.annita.dto;

import com.example.annita.model.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {

    @NotNull(message = "Tipo de voto é obrigatório")
    private VoteType type;
}
