package com.yigitusq.customer_service.service;

import com.yigitusq.customer_service.dto.AuthRequest;
import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public String authenticate(AuthRequest authRequest) {
        Customer customer = customerRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı veya şifre yanlış"));

        if (passwordEncoder.matches(authRequest.getPassword(), customer.getPassword())) {

            return "Giriş Başarılı! Token üretilecek.";
        } else {
            throw new RuntimeException("Kullanıcı bulunamadı veya şifre yanlış");
        }
    }
}
