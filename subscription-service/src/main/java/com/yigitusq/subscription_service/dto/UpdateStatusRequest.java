package com.yigitusq.subscription_service.dto;

import com.yigitusq.subscription_service.model.SubscriptionStatus;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Durum bilgisi boş olamaz")
    private SubscriptionStatus status;
}