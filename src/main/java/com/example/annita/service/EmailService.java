package com.example.annita.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;

    public EmailService(java.util.Optional<JavaMailSender> mailSender, @Value("${app.email.enabled:false}") boolean enabled) {
        this.mailSender = mailSender.orElse(null);
        this.enabled = enabled;
    }

    public void sendVerificationCode(String to, String code) {
        String subject = "Annita — Código de verificação";
        String body = "O seu código de verificação é: " + code + "\n\nEste código é válido por 15 minutos.";

        if (enabled && mailSender != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        }

        log.info("[EMAIL] Para: {} | Assunto: {} | Código: {}", to, subject, code);
    }

    public void sendEventReportedNotification(String to, String eventTitle) {
        String subject = "Annita — Evento denunciado";
        String body = "O seu evento \"" + eventTitle + "\" foi removido da plataforma após receber denúncias.";

        if (enabled && mailSender != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        }

        log.info("[EMAIL] Para: {} | Assunto: {} | Evento: {}", to, subject, eventTitle);
    }
}
