package com.yigitusq.subscription_service.client;

import com.yigitusq.customer_service.dto.DtoCustomer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {

    @GetMapping("/api/customers/{id}")
    ResponseEntity<DtoCustomer> getCustomerById(@PathVariable("id") Long id);
}