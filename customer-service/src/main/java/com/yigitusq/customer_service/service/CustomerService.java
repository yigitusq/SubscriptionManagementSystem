package com.yigitusq.customer_service.service;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.customer_service.dto.DtoCustomerIU;
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

    public DtoCustomer findById(Long id) {

        Customer customer = customerRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Customer not found - id: " + id));
        DtoCustomer dto = new DtoCustomer();
        BeanUtils.copyProperties(customer, dto);
        return dto;
    }

    public List<DtoCustomer> findAll() {
        List <DtoCustomer> dtoList = new ArrayList<>();
        List <Customer> customerList = customerRepository.findAll();
        for (Customer customer : customerList){
            DtoCustomer dto = new DtoCustomer();
            BeanUtils.copyProperties(customer,dto);
            dtoList.add(dto);
        }
        return dtoList;
    }

    public DtoCustomer save(DtoCustomerIU dtoCustomer) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(dtoCustomer, customer);

        String hashedPassword = passwordEncoder.encode(dtoCustomer.getPassword());
        customer.setPassword(hashedPassword);

        Customer dbCustomer = customerRepository.save(customer);

        System.out.println(dbCustomer.getId() + " ID'li müşteri için 'CustomerCreated' olayı gönderiliyor.");
        kafkaTemplate.send("customer-events", dbCustomer);

        DtoCustomer response = new DtoCustomer();
        BeanUtils.copyProperties(dbCustomer, response);
        return response;
    }
}
