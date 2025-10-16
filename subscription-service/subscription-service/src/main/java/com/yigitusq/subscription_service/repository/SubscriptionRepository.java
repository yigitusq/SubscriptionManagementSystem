package com.yigitusq.subscription_service.repository;

import com.yigitusq.subscription_service.model.Subscription;
import com.yigitusq.subscription_service.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    // Zamanlanmış görevin kullanacağı metot:
    // Yenileme tarihi geçmiş ve durumu AKTIF olan tüm abonelikleri bulur.
    List<Subscription> findByRenewDateBeforeAndStatus(LocalDateTime date, SubscriptionStatus status);
}
