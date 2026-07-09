package com.example.annita.dto;

import java.io.Serializable;

public record EmailMessage(
        String to,
        String subject,
        String htmlBody
) implements Serializable {
}
