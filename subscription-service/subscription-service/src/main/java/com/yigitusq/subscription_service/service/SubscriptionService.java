package com.yigitusq.subscription_service.service;


import com.fasterxml.jackson.databind.util.BeanUtil;
import com.yigitusq.subscription_service.dto.CreateSubscriptionRequest;
import com.yigitusq.subscription_service.dto.SubscriptionResponse;
import com.yigitusq.subscription_service.dto.UpdateStatusRequest;
import com.yigitusq.subscription_service.model.Offer;
import com.yigitusq.subscription_service.model.Period;
import com.yigitusq.subscription_service.model.Subscription;
import com.yigitusq.subscription_service.model.SubscriptionStatus;
import com.yigitusq.subscription_service.repository.OfferRepository;
import com.yigitusq.subscription_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final OfferRepository offerRepository;

    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request){
        Offer offer = offerRepository.findById(request.getOfferId())
                .orElseThrow(() -> new RuntimeException("Offer not found" + request.getOfferId()));


        Subscription subscription = new Subscription();
        subscription.setCustomerId(request.getCustomerId());
        subscription.setOfferId(request.getOfferId());
        subscription.setStatus(SubscriptionStatus.AKTIF);

        LocalDateTime now = LocalDateTime.now();
        if(offer.getPeriod() == Period.AYLIK){
            subscription.setRenewDate(now.plusMonths(1));
        }
        else if(offer.getPeriod() == Period.YILLIK){
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
