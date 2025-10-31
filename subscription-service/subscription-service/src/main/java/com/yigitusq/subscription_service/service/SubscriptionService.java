package com.yigitusq.subscription_service.service;


import com.yigitusq.subscription_service.client.CustomerServiceClient;
import com.yigitusq.subscription_service.dto.CreateSubscriptionRequest;
import com.yigitusq.subscription_service.dto.SubscriptionResponse;
import com.yigitusq.subscription_service.dto.UpdateStatusRequest;
import com.yigitusq.subscription_service.event.SubscriptionEventProducer;
import com.yigitusq.subscription_service.event.dto.NotificationEvent;
import com.yigitusq.subscription_service.event.dto.PaymentRequestEvent;
import com.yigitusq.subscription_service.event.dto.PaymentStatusEvent;
import com.yigitusq.subscription_service.mapper.SubscriptionMapper;
import com.yigitusq.subscription_service.model.Offer;
import com.yigitusq.subscription_service.model.Period;
import com.yigitusq.subscription_service.model.Subscription;
import com.yigitusq.subscription_service.model.SubscriptionStatus;
import com.yigitusq.subscription_service.repository.OfferRepository;
import com.yigitusq.subscription_service.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import com.yigitusq.customer_service.dto.DtoCustomer;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final OfferRepository offerRepository;
    private final OfferService offerService;
    private final CustomerServiceClient customerServiceClient;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionEventProducer eventProducer;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.notification}")
    private String notificationTopic;

    @CacheEvict(value = "subscriptions", allEntries = true)
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        // 1. Müşteri var mı diye kontrol et
        try {
            customerServiceClient.getCustomerById(request.getCustomerId());
        } catch (FeignException.NotFound ex) {
            throw new RuntimeException("Abonelik oluşturulamaz: Müşteri bulunamadı - id: " + request.getCustomerId());
        }

        // 2. Teklifi bul
        Offer offer = offerService.findById(request.getOfferId());

        // 3. Aboneliği oluştur ve PENDING olarak kaydet
        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setStatus(SubscriptionStatus.WAITINGFORPAYMENT);

        LocalDateTime now = LocalDateTime.now();
        if (offer.getPeriod() == Period.MONTHLY) {
            subscription.setRenewDate(now.plusMonths(1));
        } else if (offer.getPeriod() == Period.ANNUALLY) {
            subscription.setRenewDate(now.plusYears(1));
        }

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // 4. Kafka'ya ödeme isteği gönder
        PaymentRequestEvent event = PaymentRequestEvent.builder()
                .subscriptionId(savedSubscription.getId())
                .customerId(savedSubscription.getCustomerId())
                .amount(offer.getPrice())
                .build();
        eventProducer.sendPaymentRequest(event);

        return subscriptionMapper.toResponse(savedSubscription);
    }

    public void updateSubscriptionStatusFromEvent(PaymentStatusEvent statusEvent) {
        Subscription subscription = subscriptionRepository.findById(statusEvent.getSubscriptionId()).orElse(null);
        if (subscription == null) {
            return; // Abonelik bulunamadıysa bir şey yapma
        }

        switch (statusEvent.getStatus()) {
            case PAYMENT_SUCCESS:
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                // Başarılı ödeme sonrası bir sonraki yenileme tarihini güncelle
                Offer offer = offerService.findById(subscription.getOfferId());
                if (offer.getPeriod() == Period.MONTHLY) {
                    subscription.setRenewDate(LocalDateTime.now().plusMonths(1));
                } else if (offer.getPeriod() == Period.ANNUALLY) {
                    subscription.setRenewDate(LocalDateTime.now().plusYears(1));
                }

                sendSubscriptionStatusNotification(subscription, "Aboneliğiniz Başarıyla Aktif Edildi/Yenilendi");
                break;
            case PAYMENT_FAILED:
                subscription.setStatus(SubscriptionStatus.CANCELLED); // Veya PAYMENT_FAILED
                sendSubscriptionStatusNotification(subscription, "Abonelik Ödemeniz Başarısız Oldu");
                break;
        }
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }
    private void sendSubscriptionStatusNotification(Subscription subscription, String subject) {
        try {
            ResponseEntity<DtoCustomer> customerResponse = customerServiceClient.getCustomerById(subscription.getCustomerId());
            if (customerResponse.getStatusCode().is2xxSuccessful() && customerResponse.getBody() != null) {
                DtoCustomer customer = customerResponse.getBody();

                String message = "Merhaba " + customer.getName() + ",\n" +
                        subscription.getId() + " numaralı aboneliğinizin durumu güncellenmiştir.\n" +
                        "Yeni Durum: " + subscription.getStatus();

                NotificationEvent notificationEvent = NotificationEvent.builder()
                        .email(customer.getEmail())
                        .subject(subject)
                        .message(message)
                        .build();

                kafkaTemplate.send(notificationTopic, notificationEvent);
            }
        } catch (Exception e) {
            System.err.println("Bildirim gonderilirken hata olustu: " + e.getMessage());
        }
    }

    @CacheEvict(value = "subscriptions", allEntries = true)
    public SubscriptionResponse updateStatus(Long id, UpdateStatusRequest request) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found. Id: " + id));

        subscription.setStatus(request.getStatus());
        subscription.setUpdatedAt(LocalDateTime.now());

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(updatedSubscription);
    }
    @Cacheable(value = "subscriptions", key = "#id")
    public SubscriptionResponse getSubscriptionById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found. Id: " + id));
        return subscriptionMapper.toResponse(subscription);
    }

    @Cacheable(value = "subscriptions", key = "'all'")
    public List<SubscriptionResponse> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        return subscriptionMapper.toResponseList(subscriptions);
    }

    @CacheEvict(value = "subscriptions", allEntries = true)
    public void deleteSubscription(Long id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new RuntimeException("Subscription not found. Id: " + id);
        }
        subscriptionRepository.deleteById(id);
    }
//    private SubscriptionResponse mapToResponse(Subscription subscription) {
//        SubscriptionResponse response = new SubscriptionResponse();
//
//        BeanUtils.copyProperties(subscription, response);
//        return response;
//    }

}
