package com.yigitusq.subscription_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Aynı anda çalışacak minimum işçi sayısı
        executor.setMaxPoolSize(5);  // Maksimum işçi sayısı
        executor.setQueueCapacity(500); // Sırada bekleyebilecek görev sayısı
        executor.setThreadNamePrefix("Async-Task-"); // Loglarda tanımak için
        executor.initialize();
        return executor;
    }
}