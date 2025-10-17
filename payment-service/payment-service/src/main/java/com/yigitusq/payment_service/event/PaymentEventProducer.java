package com.yigitusq.payment_service.event;

import com.yigitusq.payment_service.dto.PaymentStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String paymentStatusTopic;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${app.kafka.topic.payment-status}") String paymentStatusTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentStatusTopic = paymentStatusTopic;
    }

    public void sendStatusEvent(PaymentStatusEvent event) {
        try {
            log.info("Ödeme sonuç event'i gönderiliyor: {}", event);
            kafkaTemplate.send(paymentStatusTopic, event);
        } catch (Exception e) {
            log.error("Ödeme sonuç event'i gönderilirken hata oluştu: {}", event, e);
        }
    }
}