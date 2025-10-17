package com.yigitusq.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestEvent {
    private String subscriptionId;
    private String userId;
    private BigDecimal amount; // Ödeme tutarı
}