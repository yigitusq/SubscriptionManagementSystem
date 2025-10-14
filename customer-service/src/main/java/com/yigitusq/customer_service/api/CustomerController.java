package com.yigitusq.customer_service.api;

import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.customer_service.repository.CustomerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("")
    public List<Customer> findAll(){
        return customerRepository.findAll();
    }

    @PostMapping("")
    public void save(@RequestBody Customer customer){
        customerRepository.save(customer);
    }
}
