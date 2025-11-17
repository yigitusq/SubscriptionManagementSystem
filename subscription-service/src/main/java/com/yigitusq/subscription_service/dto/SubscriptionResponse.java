package com.yigitusq.subscription_service.dto;

import com.yigitusq.subscription_service.model.SubscriptionStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubscriptionResponse {
    private Long id;
    private Long customerId;
    private Long offerId;
    private SubscriptionStatus status;
    private LocalDateTime renewDate;
    private LocalDateTime createdAt;
}
