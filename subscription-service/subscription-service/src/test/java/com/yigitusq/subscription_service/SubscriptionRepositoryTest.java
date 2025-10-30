package com.yigitusq.subscription_service;

import com.yigitusq.subscription_service.model.Subscription;
import com.yigitusq.subscription_service.model.SubscriptionStatus;
import com.yigitusq.subscription_service.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase; // YENİ IMPORT

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // <-- BU SATIRI EKLE
@DisplayName("Subscription Repository Tests")
class SubscriptionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Subscription subscription1;
    private Subscription subscription2;

    @BeforeEach
    void setUp() {
        subscription1 = new Subscription();
        subscription1.setCustomerId(1L);
        subscription1.setOfferId(1L);
        subscription1.setStatus(SubscriptionStatus.ACTIVE);
        subscription1.setRenewDate(LocalDateTime.now().minusDays(1)); // Geçmiş tarih
        subscription1.setCreatedAt(LocalDateTime.now());
        subscription1.setUpdatedAt(LocalDateTime.now());

        subscription2 = new Subscription();
        subscription2.setCustomerId(2L);
        subscription2.setOfferId(2L);
        subscription2.setStatus(SubscriptionStatus.ACTIVE);
        subscription2.setRenewDate(LocalDateTime.now().plusDays(30)); // Gelecek tarih
        subscription2.setCreatedAt(LocalDateTime.now());
        subscription2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should save subscription successfully")
    void testSaveSubscription() {
        // When
        Subscription saved = subscriptionRepository.save(subscription1);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should find subscription by id")
    void testFindById() {
        // Given
        Subscription saved = entityManager.persistAndFlush(subscription1);

        // When
        Optional<Subscription> found = subscriptionRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should find subscriptions by customer id")
    void testFindByCustomerId() {
        // Given
        entityManager.persist(subscription1);

        Subscription subscription3 = new Subscription();
        subscription3.setCustomerId(1L);
        subscription3.setOfferId(3L);
        subscription3.setStatus(SubscriptionStatus.FROZEN);
        subscription3.setRenewDate(LocalDateTime.now().plusDays(15));
        entityManager.persist(subscription3);
        entityManager.flush();

        // When
        List<Subscription> found = subscriptionRepository.findByCustomerId(1L);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).allMatch(s -> s.getCustomerId().equals(1L));
    }

    @Test
    @DisplayName("Should find subscriptions by status and renew date before")
    void testFindByStatusAndRenewDateBefore() {
        // Given
        entityManager.persist(subscription1); // Geçmiş tarih
        entityManager.persist(subscription2); // Gelecek tarih
        entityManager.flush();

        // When
        List<Subscription> found = subscriptionRepository
                .findByStatusAndRenewDateBefore(
                        SubscriptionStatus.ACTIVE,
                        LocalDateTime.now()
                );

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getRenewDate()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should find subscription by id and customer id")
    void testFindByIdAndCustomerId() {
        // Given
        Subscription saved = entityManager.persistAndFlush(subscription1);

        // When
        Subscription found = subscriptionRepository
                .findByIdAndCustomerId(saved.getId(), 1L);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getCustomerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return null when subscription not found by id and customer id")
    void testFindByIdAndCustomerId_NotFound() {
        // Given
        Subscription saved = entityManager.persistAndFlush(subscription1);

        // When
        Subscription found = subscriptionRepository
                .findByIdAndCustomerId(saved.getId(), 999L); // Yanlış customer ID

        // Then
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("Should update subscription")
    void testUpdateSubscription() {
        // Given
        Subscription saved = entityManager.persistAndFlush(subscription1);

        // When
        saved.setStatus(SubscriptionStatus.FROZEN);
        Subscription updated = subscriptionRepository.save(saved);
        entityManager.flush();

        // Then
        Subscription found = subscriptionRepository.findById(saved.getId()).get();
        assertThat(found.getStatus()).isEqualTo(SubscriptionStatus.FROZEN);
    }

    @Test
    @DisplayName("Should delete subscription")
    void testDeleteSubscription() {
        // Given
        Subscription saved = entityManager.persistAndFlush(subscription1);
        Long id = saved.getId();

        // When
        subscriptionRepository.deleteById(id);
        entityManager.flush();

        // Then
        Optional<Subscription> found = subscriptionRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all subscriptions")
    void testFindAll() {
        // Given
        entityManager.persist(subscription1);
        entityManager.persist(subscription2);
        entityManager.flush();

        // When
        List<Subscription> all = subscriptionRepository.findAll();

        // Then
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when finding by non-existent customer id")
    void testFindByCustomerId_Empty() {
        // When
        List<Subscription> found = subscriptionRepository.findByCustomerId(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should count subscriptions")
    void testCount() {
        // Given
        entityManager.persist(subscription1);
        entityManager.persist(subscription2);
        entityManager.flush();

        // When
        long count = subscriptionRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}