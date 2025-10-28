package com.yigitusq.customer_service.controller;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.customer_service.dto.DtoCustomerIU;
import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.customer_service.service.CustomerService;
//import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<DtoCustomer> getAll(){
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DtoCustomer> getCustomerById(@PathVariable Long id) {
        DtoCustomer customerDto = customerService.findById(id);
        return ResponseEntity.ok(customerDto);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public DtoCustomer createCustomer(@Valid @RequestBody DtoCustomerIU dtoCustomerIU) {
        return customerService.save(dtoCustomerIU);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteById(id);
    }
}//    @PostMapping
//    public ResponseEntity<DtoCustomer> createCustomer(@RequestBody DtoCustomerIU dtoCustomerIU) {
//        DtoCustomerIU savedCustomer = customerService.saveCustomer(dtoCustomerIU);
//        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);