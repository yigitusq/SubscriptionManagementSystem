package com.yigitusq.subscription_service.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusEvent {
    private Long subscriptionId;
    private PaymentStatus status;
    private String transactionId;
}