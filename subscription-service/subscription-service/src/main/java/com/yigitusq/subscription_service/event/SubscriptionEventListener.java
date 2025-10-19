package com.yigitusq.subscription_service.event;

import com.yigitusq.subscription_service.event.dto.PaymentStatusEvent;
import com.yigitusq.subscription_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionEventListener {

    private final SubscriptionService subscriptionService;

    @KafkaListener(topics = "${app.kafka.topic.payment-status}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentStatus(PaymentStatusEvent statusEvent) {
        log.info("Ödeme sonuç event'i yakalandı: {}", statusEvent);
        subscriptionService.updateSubscriptionStatusFromEvent(statusEvent);
    }
}
