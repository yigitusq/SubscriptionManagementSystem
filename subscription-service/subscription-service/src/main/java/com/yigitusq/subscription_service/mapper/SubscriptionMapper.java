package com.yigitusq.subscription_service.mapper;

import com.yigitusq.subscription_service.dto.CreateSubscriptionRequest;
import com.yigitusq.subscription_service.dto.SubscriptionResponse;
import com.yigitusq.subscription_service.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    SubscriptionResponse toResponse(Subscription subscription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "renewDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Subscription toEntity(CreateSubscriptionRequest request);
    List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions);
}
