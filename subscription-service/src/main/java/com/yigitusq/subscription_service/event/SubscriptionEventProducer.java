package com.yigitusq.subscription_service.event;

import com.yigitusq.subscription_service.event.dto.PaymentRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.payment-request}")
    private String paymentRequestTopic;

    public void sendPaymentRequest(PaymentRequestEvent event) {
        try {
            log.info("Ödeme isteği gönderiliyor: {}", event);
            kafkaTemplate.send(paymentRequestTopic, event);
        } catch (Exception e) {
            log.error("Ödeme isteği gönderilirken hata oluştu: {}", event, e);
        }
    }
}