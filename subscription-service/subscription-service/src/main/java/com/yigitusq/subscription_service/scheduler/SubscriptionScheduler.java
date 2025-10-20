package com.yigitusq.subscription_service.scheduler;

import com.yigitusq.subscription_service.event.SubscriptionEventProducer;
import com.yigitusq.subscription_service.event.dto.PaymentRequestEvent;
import com.yigitusq.subscription_service.model.Offer;
import com.yigitusq.subscription_service.model.Subscription;
import com.yigitusq.subscription_service.model.SubscriptionStatus;
import com.yigitusq.subscription_service.repository.OfferRepository;
import com.yigitusq.subscription_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final OfferRepository offerRepository;
    private final SubscriptionEventProducer eventProducer;

    /**
     * Bu metot her gün sabah 02:00'de otomatik olarak çalışır.
     * Cron expression: saniye, dakika, saat, gün, ay, haftanın günü
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkAndProcessRenewals() {
        log.info("Abonelik yenileme kontrolü başlatılıyor...");

        List<Subscription> subscriptionsToRenew = subscriptionRepository
                .findByStatusAndRenewDateBefore(SubscriptionStatus.ACTIVE, LocalDateTime.now());

        if (subscriptionsToRenew.isEmpty()) {
            log.info("Yenilenecek abonelik bulunamadı.");
            return;
        }

        log.info("{} adet yenilenecek abonelik bulundu.", subscriptionsToRenew.size());

        for (Subscription subscription : subscriptionsToRenew) {
            try {
                Offer offer = offerRepository.findById(subscription.getOfferId()).orElseThrow();

                PaymentRequestEvent event = PaymentRequestEvent.builder()
                        .subscriptionId(subscription.getId())
                        .customerId(subscription.getCustomerId())
                        .amount(offer.getPrice())
                        .build();

                eventProducer.sendPaymentRequest(event);

                subscription.setStatus(SubscriptionStatus.WAITINGFORPAYMENT);
                subscriptionRepository.save(subscription);

                log.info("{} ID'li abonelik için yenileme ödeme isteği gönderildi.", subscription.getId());
            } catch (Exception e) {
                log.error("{} ID'li abonelik yenileme işlemi sırasında hata oluştu: {}", subscription.getId(), e.getMessage());
            }
        }
    }
}