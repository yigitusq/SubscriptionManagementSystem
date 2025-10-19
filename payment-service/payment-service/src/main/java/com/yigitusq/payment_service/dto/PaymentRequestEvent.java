package com.yigitusq.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestEvent {
    private Long subscriptionId;
    private Long customerId;
    private BigDecimal amount;
}