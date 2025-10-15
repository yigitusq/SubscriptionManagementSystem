package com.yigitusq.customer_service.service;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.customer_service.dto.DtoCustomerIU;
import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.customer_service.repository.CustomerRepository;
//import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {


    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public DtoCustomer findById(int id) {

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
        DtoCustomer response = new DtoCustomer();
        Customer customer = new Customer();
        BeanUtils.copyProperties(dtoCustomer,customer);
        //geçici
        System.out.println("Kaydedilecek Entity: " + customer.toString());

        Customer dbCustomer = customerRepository.save(customer);
        BeanUtils.copyProperties(dbCustomer,response);
        return response;
    }
}
