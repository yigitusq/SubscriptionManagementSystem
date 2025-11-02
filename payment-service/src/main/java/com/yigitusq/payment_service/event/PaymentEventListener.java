package com.yigitusq.payment_service.event;

import com.yigitusq.payment_service.dto.PaymentRequestEvent;
import com.yigitusq.payment_service.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);
    private final PaymentService paymentService;

    public PaymentEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "${app.kafka.topic.payment-request}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentRequest(PaymentRequestEvent requestEvent) {
        log.info("Kafka'dan ödeme isteği event'i yakalandı: {}", requestEvent);
        paymentService.processPayment(requestEvent);
    }
}