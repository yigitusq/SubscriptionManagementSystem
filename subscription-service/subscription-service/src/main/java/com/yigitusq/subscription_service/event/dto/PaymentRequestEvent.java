package com.yigitusq.subscription_service.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestEvent {
    private Long subscriptionId;
    private Long customerId;
    private BigDecimal amount;
}