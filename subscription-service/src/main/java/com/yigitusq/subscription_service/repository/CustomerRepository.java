package com.yigitusq.subscription_service.repository;

import com.yigitusq.subscription_service.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
