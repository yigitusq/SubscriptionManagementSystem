package com.yigitusq.subscription_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Kural 1: Gelen HERHANGİ BİR isteğe kimlik sormadan İZİN VER.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // Kural 2: CSRF korumasını kapat (REST API'ler için standarttır).
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}