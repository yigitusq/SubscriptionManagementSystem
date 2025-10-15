package com.yigitusq.customer_service.dto;

import lombok.Data;

@Data // Getter, Setter, toString vs. için Lombok
public class CustomerRequestDTO {
    private String name;
    private String username;
    private String email;
}