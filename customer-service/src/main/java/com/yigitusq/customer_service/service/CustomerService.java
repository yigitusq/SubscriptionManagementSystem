package com.yigitusq.customer_service.service;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.customer_service.dto.DtoCustomerIU;
import com.yigitusq.customer_service.mapper.CustomerMapper;
import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.customer_service.repository.CustomerRepository;
//import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.yigitusq.customer_service.event.dto.NotificationEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

//import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;

    @Value("${app.kafka.topic.notification}")
    private String notificationTopic;

    @Cacheable(value = "customers", key = "#id")
    public DtoCustomer findById(Long id) {

        Customer customer = customerRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Customer not found - id: " + id));
        DtoCustomer dto = new DtoCustomer();
        return customerMapper.toDto(customer);
    }

    @Cacheable(value = "customers", key = "'all'")
    public List<DtoCustomer> findAll() {
        List<Customer> customerList = customerRepository.findAll();
        return customerMapper.toDtoList(customerList);
    }

    @Caching(
            put = {
                    @CachePut(value = "customers", key = "#result.id")
            },
            evict = {
                    @CacheEvict(value = "customers", key = "'all'")
            }
    )
     public DtoCustomer save(DtoCustomerIU dtoCustomer) {
        Customer customer = customerMapper.toEntity(dtoCustomer);

        String hashedPassword = passwordEncoder.encode(dtoCustomer.getPassword());
        customer.setPassword(hashedPassword);

        Customer dbCustomer = customerRepository.save(customer);

        System.out.println(dbCustomer.getId() + " ID'li müşteri için 'CustomerCreated' olayı gönderiliyor.");
        kafkaTemplate.send("customer-events", dbCustomer);

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .to(dbCustomer.getEmail())
                .subject("Aramıza Hoş Geldiniz!")
                .message("Merhaba " + dbCustomer.getName() + ", hesabınız başarıyla oluşturuldu.")
                .build();

        System.out.println(dbCustomer.getEmail() + " adresine 'Hoş Geldin' bildirimi gönderiliyor.");
        kafkaTemplate.send(notificationTopic, notificationEvent);

        return customerMapper.toDto(dbCustomer);
    }

    @CacheEvict(value = "customers", key = "#id")
    public void deleteById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found - id: " + id));

        customer.setStatus("INACTIVE");
        customerRepository.save(customer);

        //kafkaTemplate.send("customer-events-deleted", customer);
    }
}
