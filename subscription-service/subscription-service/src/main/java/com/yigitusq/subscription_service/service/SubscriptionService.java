package com.yigitusq.subscription_service.service;


import com.yigitusq.subscription_service.dto.CreateSubscriptionRequest;
import com.yigitusq.subscription_service.dto.SubscriptionResponse;
import com.yigitusq.subscription_service.dto.UpdateStatusRequest;
import com.yigitusq.subscription_service.model.Offer;
import com.yigitusq.subscription_service.model.Period;
import com.yigitusq.subscription_service.model.Subscription;
import com.yigitusq.subscription_service.model.SubscriptionStatus;
import com.yigitusq.subscription_service.repository.CustomerRepository;
import com.yigitusq.subscription_service.repository.OfferRepository;
import com.yigitusq.subscription_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import com.yigitusq.subscription_service.client.CustomerServiceClient; // Yeni Feign Client'ı import et
import feign.FeignException; // Feign'in kendi exception sınıfı

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final OfferRepository offerRepository;
    private final CustomerServiceClient customerServiceClient;

    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        try {
            customerServiceClient.getCustomerById(request.getCustomerId());
        } catch (FeignException.NotFound ex) {
            throw new RuntimeException("Abonelik oluşturulamaz: Müşteri bulunamadı - id: " + request.getCustomerId());
        }

        Offer offer = offerRepository.findById(request.getOfferId())
                .orElseThrow(() -> new RuntimeException("Teklif bulunamadı: " + request.getOfferId()));
        Subscription subscription = new Subscription();
        subscription.setCustomerId(request.getCustomerId());
        subscription.setOfferId(request.getOfferId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        if(offer.getPeriod() == Period.MONTHLY){
            subscription.setRenewDate(now.plusMonths(1));
        }
        else if(offer.getPeriod() == Period.ANNUALLY){
            subscription.setRenewDate(now.plusYears(1));
        }

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return mapToResponse(savedSubscription);
    }

    public SubscriptionResponse updateStatus(Long id, UpdateStatusRequest request) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found. Id: " + id));

        subscription.setStatus(request.getStatus());
        subscription.setUpdatedAt(LocalDateTime.now());

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return mapToResponse(updatedSubscription);
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();

        //TODO: Beanutils kullanmadan yap
        BeanUtils.copyProperties(subscription, response);
        return response;
    }

}
