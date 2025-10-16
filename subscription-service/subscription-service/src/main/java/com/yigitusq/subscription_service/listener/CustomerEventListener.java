package com.yigitusq.subscription_service.listener;

import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.subscription_service.repository.CustomerRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventListener {

    private final CustomerRepository customerRepository;

    public CustomerEventListener(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @KafkaListener(topics = "customer-events", groupId = "subscription_service_group")
    public void handleCustomerCreatedEvent(Customer customerEvent) {
        System.out.println(customerEvent.getId() + " ID'li müşteri olayı alındı. Veritabanına kopyalanıyor...");

        com.yigitusq.subscription_service.model.Customer customerView = new com.yigitusq.subscription_service.model.Customer();

        customerView.setId(customerEvent.getId());
        customerView.setName(customerEvent.getName());
        customerView.setEmail(customerEvent.getEmail());

        customerRepository.save(customerView);
        System.out.println("Müşteri kopyalandı.");
    }
}