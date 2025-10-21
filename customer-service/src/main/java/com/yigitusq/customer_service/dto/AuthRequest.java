package com.yigitusq.customer_service.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
