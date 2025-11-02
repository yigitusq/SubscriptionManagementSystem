package com.yigitusq.subscription_service.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerViewMapper {

    com.yigitusq.subscription_service.model.Customer toView(com.yigitusq.customer_service.model.Customer customerEvent);

}