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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import com.yigitusq.customer_service.dto.DtoCustomer;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final OfferRepository offerRepository;
    private final CustomerServiceClient customerServiceClient;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionEventProducer eventProducer;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.notification}")
    private String notificationTopic;

    private final Executor taskExecutor;

    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        log.info("Subscription oluşturma işlemi başladı...");

        // GÖREV 1: Müşteri kontrolünü asenkron olarak başlat
        // (Bu görev bir sonuç döndürmez, sadece başarılıysa biter, başarısızsa hata fırlatır)
        CompletableFuture<Void> customerCheckFuture = CompletableFuture.runAsync(() -> {
            log.info("Async: Müşteri varlığı kontrol ediliyor (ID: {})...", request.getCustomerId());
            try {
                customerServiceClient.getCustomerById(request.getCustomerId());
            } catch (FeignException.NotFound ex) {
                // Hata fırlat ki ana thread bunu yakalasın
                throw new CompletionException(new RuntimeException("Abonelik oluşturulamaz: Müşteri bulunamadı - id: " + request.getCustomerId()));
            } catch (Exception e) {
                throw new CompletionException(new RuntimeException("Müşteri servisiyle konuşurken hata oluştu: " + e.getMessage()));
            }
        }, taskExecutor);

        CompletableFuture<Offer> offerFuture = CompletableFuture.supplyAsync(() -> {
            log.info("Async: Teklif detayları getiriliyor (ID: {})...", request.getOfferId());
            return offerRepository.findById(request.getOfferId())
                    .orElseThrow(() -> new CompletionException(new RuntimeException("Teklif bulunamadı: " + request.getOfferId())));
        }, taskExecutor);

        try {
            CompletableFuture.allOf(customerCheckFuture, offerFuture).join();
        } catch (CompletionException e) {
            // görevlerden biri başarısız olursa (Müşteri yoksa VEYA Teklif yoksa)
            // hatayı yakala ve işlemi durdur.
            log.error("Asenkron görevlerden biri başarısız oldu: {}", e.getCause().getMessage());
            throw (RuntimeException) e.getCause();
        }

        // her iki görev de başarıyla bittiyse buraya gelinir
        log.info("Her iki asenkron görev de tamamlandı. Abonelik oluşturuluyor.");

        Offer offer = offerFuture.join();

        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setStatus(SubscriptionStatus.WAITINGFORPAYMENT); // << DEĞİŞİKLİK: Başlangıç durumu PENDING

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
                Offer offer = offerRepository.findById(subscription.getOfferId()).orElseThrow();
                if (offer.getPeriod() == Period.MONTHLY) {
                    subscription.setRenewDate(LocalDateTime.now().plusMonths(1));
                } else if (offer.getPeriod() == Period.ANNUALLY) {
                    subscription.setRenewDate(LocalDateTime.now().plusYears(1));
                }

                sendSubscriptionStatusNotification(subscription, "Aboneliginiz Basariyla Aktif Edildi/Yenilendi");
                break;
            case PAYMENT_FAILED:
                subscription.setStatus(SubscriptionStatus.CANCELLED); // Veya PAYMENT_FAILED
                sendSubscriptionStatusNotification(subscription, "Abonelik Odemeniz Basarisiz Oldu");
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
                        subscription.getId() + " numarali aboneliginizin durumu guncellenmistir.\n" +
                        "Yeni Durum: " + subscription.getStatus();

                NotificationEvent notificationEvent = NotificationEvent.builder()
                        .email(customer.getEmail())
                        .subject(subject)
                        .message(message)
                        .build();

                kafkaTemplate.send(notificationTopic, notificationEvent);
            }
        } catch (Exception e) {
            // Müşteri bulunamazsa veya başka bir hata olursa logla, ama sistemi durdurma.
            System.err.println("Bildirim gonderilirken hata olustu: " + e.getMessage());
        }
    }

    public SubscriptionResponse updateStatus(Long id, UpdateStatusRequest request) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found. Id: " + id));

        subscription.setStatus(request.getStatus());
        subscription.setUpdatedAt(LocalDateTime.now());

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(updatedSubscription);
    }

    public SubscriptionResponse getSubscriptionById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found. Id: " + id));
        return subscriptionMapper.toResponse(subscription);
    }

    public List<SubscriptionResponse> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        return subscriptionMapper.toResponseList(subscriptions);
    }


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
