package com.example.annita.service;

import com.example.annita.dto.EmailMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RabbitTemplate rabbitTemplate;
    private final String emailExchange;
    private final String emailRoutingKey;
    private final String logoUrl;
    private final Resource templateResource;

    private String templateHtml;

    public EmailService(RabbitTemplate rabbitTemplate,
                        @Value("${app.rabbitmq.email.exchange:email.exchange}") String emailExchange,
                        @Value("${app.rabbitmq.email.routing-key:email.routing-key}") String emailRoutingKey,
                        @Value("${LOGO_URL:}") String logoUrl,
                        @Value("classpath:templates/email-template.html") Resource templateResource) {
        this.rabbitTemplate = rabbitTemplate;
        this.emailExchange = emailExchange;
        this.emailRoutingKey = emailRoutingKey;
        this.logoUrl = logoUrl;
        this.templateResource = templateResource;
    }

    @PostConstruct
    public void init() {
        try {
            templateHtml = templateResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Erro ao carregar template de email", e);
            templateHtml = "<html><body>{{content}}</body></html>";
        }
    }

    public void sendVerificationCode(String to, String code) {
        String content = """
                <p>O seu código de verificação é:</p>
                <div class="code">%s</div>
                <p>Este código é válido por <strong>15 minutos</strong>.</p>
                """.formatted(code);
        send(to, "Código de verificação", content);
    }

    public void sendEventReportedNotification(String to, String eventTitle) {
        String content = """
                <p>O seu evento <strong>%s</strong> foi removido da plataforma após receber denúncias.</p>
                <p>Se tiver dúvidas, entre em contacto com a nossa equipa.</p>
                """.formatted(eventTitle);
        send(to, "Evento denunciado", content);
    }

    public void sendNewEventNotification(String to, String eventTitle, String eventDescription, String eventLink) {
        String content = """
                <p>Um novo evento foi publicado na Annita!</p>
                <p><strong>%s</strong></p>
                <p>%s</p>
                <p style="text-align:center">
                  <a class="btn" href="%s">Ver evento</a>
                </p>
                """.formatted(eventTitle, eventDescription, eventLink);
        send(to, "Novo evento: " + eventTitle, content);
    }

    private void send(String to, String title, String content) {
        String html = templateHtml
                .replace("{{logoUrl}}", logoUrl)
                .replace("{{title}}", title)
                .replace("{{content}}", content);

        EmailMessage message = new EmailMessage(to, "Annita — " + title, html);
        log.info("[EMAIL] Publicando na fila: Para: {} | Assunto: {}", message.to(), message.subject());
        rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, message);
    }
}
