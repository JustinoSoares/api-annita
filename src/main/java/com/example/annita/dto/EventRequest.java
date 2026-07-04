package com.example.annita.dto;

import com.example.annita.model.EventModality;
import com.example.annita.model.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EventRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @Size(max = 500, message = "Link deve ter no máximo 500 caracteres")
    private String link;

    @NotNull(message = "Categoria é obrigatória")
    private UUID categoryId;

    @NotNull(message = "Modalidade é obrigatória")
    private EventModality modality;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime startDate;

    @NotNull(message = "Tipo é obrigatório")
    private EventType type;

    @Size(max = 500, message = "URL da imagem de capa deve ter no máximo 500 caracteres")
    private String coverImage;

    @Size(max = 500, message = "Localização deve ter no máximo 500 caracteres")
    private String location;
}
