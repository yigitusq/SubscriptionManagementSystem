package com.yigitusq.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusEvent {
    private String subscriptionId;
    private PaymentStatus status;
    private String transactionId; // Simüle edilmiş bir işlem ID'si
}