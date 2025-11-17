package com.yigitusq.customer_service.controller;

import com.yigitusq.customer_service.dto.AuthRequest;
import com.yigitusq.customer_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest authRequest) {
        String token = authService.authenticate(authRequest);
        Map<String, String> response = Map.of("token", token); // Token'ı bir Map'e koy
        return ResponseEntity.ok(response); // <-- Artık JSON dönecek
    }
}