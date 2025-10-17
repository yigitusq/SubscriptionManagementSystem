package com.yigitusq.customer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoCustomer {

    private Long id;

    private String name;

    private String surname;

    private String email;

    private String status;

}