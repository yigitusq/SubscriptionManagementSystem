
package com.yigitusq.subscription_service.dto;

import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    private Long customerId;
    private Long offerId;
}