package com.example.annita.service;

import com.example.annita.dto.EmailMessage;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;

    public EmailConsumer(java.util.Optional<JavaMailSender> mailSender,
                         @Value("${app.email.enabled:false}") boolean enabled) {
        this.mailSender = mailSender.orElse(null);
        this.enabled = enabled;
    }

    @RabbitListener(queues = "${app.rabbitmq.email.queue:email.queue}")
    public void handleEmail(EmailMessage message) {
        log.info("[EMAIL] Para: {} | Assunto: {}", message.to(), message.subject());

        if (!enabled || mailSender == null) {
            log.info("[EMAIL] Envio desativado ou mailSender não configurado.");
            return;
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.htmlBody(), true);
            mailSender.send(mime);
        } catch (Exception e) {
            log.error("[EMAIL] Falha ao enviar email para {}: {}", message.to(), e.getMessage());
            throw new RuntimeException("Falha no envio de email", e);
        }
    }
}
