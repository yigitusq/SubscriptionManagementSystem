package com.yigitusq.customer_service.controller;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.customer_service.dto.DtoCustomerIU;
import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.customer_service.service.CustomerService;
//import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/customers")//finalleri otomatik constructor oluşturur
public class CustomerController {



    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> getAll(){
        return customerService.findAll();
    }

//    @PostMapping
//    public ResponseEntity<DtoCustomer> createCustomer(@RequestBody DtoCustomerIU dtoCustomerIU) {
//        DtoCustomerIU savedCustomer = customerService.saveCustomer(dtoCustomerIU);
//        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public DtoCustomer createCustomer(@RequestBody DtoCustomerIU dtoCustomerIU) {
        return customerService.save(dtoCustomerIU);
    }
}