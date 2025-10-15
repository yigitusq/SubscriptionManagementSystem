package com.yigitusq.customer_service.service;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.customer_service.dto.DtoCustomerIU;
import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.customer_service.repository.CustomerRepository;
//import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {


    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public DtoCustomer save(DtoCustomerIU dtoCustomer) {
        DtoCustomer response = new DtoCustomer();
        Customer customer = new Customer();
        BeanUtils.copyProperties(dtoCustomer,customer);

        Customer dbCustomer = customerRepository.save(customer);
        BeanUtils.copyProperties(dbCustomer,response);
        return response;
    }
}
