package com.yigitusq.subscription_service.listener;

import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.subscription_service.mapper.CustomerViewMapper;
import com.yigitusq.subscription_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    private final CustomerRepository customerRepository;
    private final CustomerViewMapper customerViewMapper;

    @KafkaListener(topics = "customer-events", groupId = "subscription_service_group")
    public void handleCustomerCreatedEvent(Customer customerEvent) {
        System.out.println(customerEvent.getId() + " ID'li müşteri olayı alındı. Veritabanına kopyalanıyor...");

        com.yigitusq.subscription_service.model.Customer customerView = customerViewMapper.toView(customerEvent);

        customerRepository.save(customerView);
        System.out.println("Müşteri kopyalandı.");

    }
}