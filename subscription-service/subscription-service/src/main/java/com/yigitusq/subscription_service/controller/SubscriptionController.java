package com.yigitusq.subscription_service.controller;

import com.yigitusq.subscription_service.dto.CreateSubscriptionRequest;
import com.yigitusq.subscription_service.dto.SubscriptionResponse;
import com.yigitusq.subscription_service.dto.UpdateStatusRequest;
import com.yigitusq.subscription_service.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(@PathVariable Long id) {
        SubscriptionResponse response = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        List<SubscriptionResponse> responses = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SubscriptionResponse> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        SubscriptionResponse response = subscriptionService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
    }
}