package com.example.annita.service;

import com.example.annita.dto.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RabbitTemplate rabbitTemplate;
    private final String emailExchange;
    private final String emailRoutingKey;

    public EmailService(RabbitTemplate rabbitTemplate,
                        @Value("${app.rabbitmq.email.exchange:email.exchange}") String emailExchange,
                        @Value("${app.rabbitmq.email.routing-key:email.routing-key}") String emailRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.emailExchange = emailExchange;
        this.emailRoutingKey = emailRoutingKey;
    }

    public void sendVerificationCode(String to, String code) {
        EmailMessage message = EmailMessage.verificationCode(to, code);
        publish(message);
    }

    public void sendEventReportedNotification(String to, String eventTitle) {
        EmailMessage message = EmailMessage.eventReported(to, eventTitle);
        publish(message);
    }

    public void sendNewEventNotification(String to, String eventTitle, String eventDescription, String eventLink) {
        EmailMessage message = EmailMessage.newEvent(to, eventTitle, eventDescription, eventLink);
        publish(message);
    }

    private void publish(EmailMessage message) {
        log.info("[EMAIL] Publicando na fila: Para: {} | Assunto: {}", message.to(), message.subject());
        rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, message);
    }
}
