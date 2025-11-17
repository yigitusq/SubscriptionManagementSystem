package com.yigitusq.subscription_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "subscriptions")
public class Subscription implements java.io.Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId; // Diğer microservisin ID'sini direkt olarak saklıyoruz

    @Column(name = "offer_id", nullable = false)
    private Long offerId;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Column(name = "renew_date")
    private LocalDateTime renewDate; // Bir sonraki yenileme/ödeme tarihi

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}