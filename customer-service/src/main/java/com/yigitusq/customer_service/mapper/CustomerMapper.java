package com.yigitusq.customer_service.mapper;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.customer_service.dto.DtoCustomerIU;
import com.yigitusq.customer_service.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    DtoCustomer toDto(Customer customer);

    List<DtoCustomer> toDtoList(List<Customer> customers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(DtoCustomerIU dtoCustomerIU);

}