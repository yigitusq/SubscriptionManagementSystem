package com.yigitusq.customer_service.service;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.customer_service.dto.DtoCustomerIU;
import com.yigitusq.customer_service.mapper.CustomerMapper;
import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.customer_service.repository.CustomerRepository;
//import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;

    public DtoCustomer findById(Long id) {

        Customer customer = customerRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Customer not found - id: " + id));
        DtoCustomer dto = new DtoCustomer();
        return customerMapper.toDto(customer);
    }

    public List<DtoCustomer> findAll() {
        List<Customer> customerList = customerRepository.findAll();
        return customerMapper.toDtoList(customerList);
    }

    public DtoCustomer save(DtoCustomerIU dtoCustomer) {
        Customer customer = customerMapper.toEntity(dtoCustomer);

        String hashedPassword = passwordEncoder.encode(dtoCustomer.getPassword());
        customer.setPassword(hashedPassword);

        Customer dbCustomer = customerRepository.save(customer);

        System.out.println(dbCustomer.getId() + " ID'li müşteri için 'CustomerCreated' olayı gönderiliyor.");
        kafkaTemplate.send("customer-events", dbCustomer);

        return customerMapper.toDto(dbCustomer);
    }

    public void deleteById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found - id: " + id));

        customer.setStatus("INACTIVE");
        customerRepository.save(customer);

        //kafkaTemplate.send("customer-events-deleted", customer);
    }
}
