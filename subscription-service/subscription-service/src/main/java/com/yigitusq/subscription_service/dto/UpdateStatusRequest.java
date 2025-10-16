package com.yigitusq.subscription_service.dto;

import com.yigitusq.subscription_service.model.SubscriptionStatus;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    private SubscriptionStatus status;
}