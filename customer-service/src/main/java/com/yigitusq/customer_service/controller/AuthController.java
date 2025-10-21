package com.yigitusq.customer_service.controller;

import com.yigitusq.customer_service.dto.AuthRequest;
import com.yigitusq.customer_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {

        String response = authService.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }
}