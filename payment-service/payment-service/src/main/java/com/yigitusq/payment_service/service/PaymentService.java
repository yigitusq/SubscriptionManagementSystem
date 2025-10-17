package com.yigitusq.payment_service.service;

import com.yigitusq.payment_service.dto.PaymentRequestEvent;
import com.yigitusq.payment_service.dto.PaymentStatus;
import com.yigitusq.payment_service.dto.PaymentStatusEvent;
import com.yigitusq.payment_service.event.PaymentEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentEventProducer paymentEventProducer;
    private final Random random = new Random();

    public PaymentService(PaymentEventProducer paymentEventProducer) {
        this.paymentEventProducer = paymentEventProducer;
    }

    public void processPayment(PaymentRequestEvent requestEvent) {
        log.info("Ödeme isteği alındı: SubscriptionId={}", requestEvent.getSubscriptionId());

        // Ödeme işlemini simüle et: %50 başarı, %50 başarısızlık
        boolean isSuccess = random.nextBoolean();

        PaymentStatus status = isSuccess ? PaymentStatus.PAYMENT_SUCCESS : PaymentStatus.PAYMENT_FAILED;
        log.info("Ödeme sonucu: {}", status);

        // Sonuç event'ini oluştur
        PaymentStatusEvent statusEvent = PaymentStatusEvent.builder()
                .subscriptionId(requestEvent.getSubscriptionId())
                .status(status)
                .transactionId(UUID.randomUUID().toString()) // Rastgele bir transaction ID
                .build();

        // Sonucu Kafka'ya gönder
        paymentEventProducer.sendStatusEvent(statusEvent);
    }
}