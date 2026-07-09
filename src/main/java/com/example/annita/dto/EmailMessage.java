package com.example.annita.dto;

import java.io.Serializable;

public record EmailMessage(
        String to,
        String subject,
        String body
) implements Serializable {
    public static EmailMessage verificationCode(String to, String code) {
        return new EmailMessage(
                to,
                "Annita — Código de verificação",
                "O seu código de verificação é: " + code + "\n\nEste código é válido por 15 minutos."
        );
    }

    public static EmailMessage eventReported(String to, String eventTitle) {
        return new EmailMessage(
                to,
                "Annita — Evento denunciado",
                "O seu evento \"" + eventTitle + "\" foi removido da plataforma após receber denúncias."
        );
    }

    public static EmailMessage newEvent(String to, String eventTitle, String eventDescription, String eventLink) {
        return new EmailMessage(
                to,
                "Annita — Novo evento: " + eventTitle,
                "Um novo evento foi publicado na Annita!\n\n"
                        + "Título: " + eventTitle + "\n"
                        + "Descrição: " + eventDescription + "\n"
                        + "Link: " + eventLink + "\n\n"
                        + "Acesse a plataforma para mais detalhes."
        );
    }
}
