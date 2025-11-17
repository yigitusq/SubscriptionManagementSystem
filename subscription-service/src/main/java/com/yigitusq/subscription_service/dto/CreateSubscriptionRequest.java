
package com.yigitusq.subscription_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {

    @NotNull(message = "Müşteri ID boş olamaz")
    @Positive(message = "Müşteri ID pozitif bir sayı olmalıdır")
    private Long customerId;

    @NotNull(message = "Teklif ID boş olamaz")
    @Positive(message = "Teklif ID pozitif bir sayı olmalıdır")
    private Long offerId;
}