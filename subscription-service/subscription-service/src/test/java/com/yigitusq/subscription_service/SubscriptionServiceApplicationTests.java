package com.yigitusq.subscription_service;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.subscription_service.client.CustomerServiceClient;
import com.yigitusq.subscription_service.dto.CreateSubscriptionRequest;
import com.yigitusq.subscription_service.dto.SubscriptionResponse;
import com.yigitusq.subscription_service.dto.UpdateStatusRequest;
import com.yigitusq.subscription_service.event.SubscriptionEventProducer;
import com.yigitusq.subscription_service.event.dto.PaymentRequestEvent;
import com.yigitusq.subscription_service.event.dto.PaymentStatus;
import com.yigitusq.subscription_service.event.dto.PaymentStatusEvent;
import com.yigitusq.subscription_service.mapper.SubscriptionMapper;
import com.yigitusq.subscription_service.model.Offer;
import com.yigitusq.subscription_service.model.Period;
import com.yigitusq.subscription_service.model.Subscription;
import com.yigitusq.subscription_service.model.SubscriptionStatus;
import com.yigitusq.subscription_service.repository.SubscriptionRepository;
import com.yigitusq.subscription_service.service.OfferService;
import com.yigitusq.subscription_service.service.SubscriptionService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Subscription Service Tests")
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private OfferService offerService;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private SubscriptionEventProducer eventProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Subscription subscription;
    private Offer offer;
    private CreateSubscriptionRequest createRequest;
    private SubscriptionResponse subscriptionResponse;
    private DtoCustomer dtoCustomer;

    @BeforeEach
    void setUp() {
        // @Value field'ı mock için set et
        ReflectionTestUtils.setField(subscriptionService, "notificationTopic", "notification-topic");

        // Test verileri hazırlama
        offer = new Offer();
        offer.setId(1L);
        offer.setName("Premium Plan");
        offer.setPrice(new BigDecimal("99.99"));
        offer.setPeriod(Period.MONTHLY);

        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setCustomerId(1L);
        subscription.setOfferId(1L);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setRenewDate(LocalDateTime.now().plusMonths(1));
        subscription.setCreatedAt(LocalDateTime.now());

        createRequest = new CreateSubscriptionRequest();
        createRequest.setCustomerId(1L);
        createRequest.setOfferId(1L);

        subscriptionResponse = new SubscriptionResponse();
        subscriptionResponse.setId(1L);
        subscriptionResponse.setCustomerId(1L);
        subscriptionResponse.setOfferId(1L);
        subscriptionResponse.setStatus(SubscriptionStatus.WAITINGFORPAYMENT);

        dtoCustomer = new DtoCustomer();
        dtoCustomer.setId(1L);
        dtoCustomer.setName("John");
        dtoCustomer.setSurname("Doe");
        dtoCustomer.setEmail("john.doe@example.com");
        dtoCustomer.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should create subscription successfully")
    void testCreateSubscription_Success() {
        // Given
        when(customerServiceClient.getCustomerById(anyLong()))
                .thenReturn(ResponseEntity.ok(dtoCustomer));
        when(offerService.findById(anyLong())).thenReturn(offer);
        when(subscriptionMapper.toEntity(any(CreateSubscriptionRequest.class)))
                .thenReturn(subscription);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(subscription);
        when(subscriptionMapper.toResponse(any(Subscription.class)))
                .thenReturn(subscriptionResponse);

        // When
        SubscriptionResponse result = subscriptionService.createSubscription(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.WAITINGFORPAYMENT);

        // Verify interactions
        verify(customerServiceClient, times(1)).getCustomerById(1L);
        verify(offerService, times(1)).findById(1L);
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(eventProducer, times(1)).sendPaymentRequest(any(PaymentRequestEvent.class));

        // Verify payment request event
        ArgumentCaptor<PaymentRequestEvent> eventCaptor =
                ArgumentCaptor.forClass(PaymentRequestEvent.class);
        verify(eventProducer).sendPaymentRequest(eventCaptor.capture());
        PaymentRequestEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getCustomerId()).isEqualTo(1L);
        assertThat(capturedEvent.getAmount()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void testCreateSubscription_CustomerNotFound() {
        // Given
        when(customerServiceClient.getCustomerById(anyLong()))
                .thenThrow(FeignException.NotFound.class);

        // When & Then
        assertThatThrownBy(() -> subscriptionService.createSubscription(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Müşteri bulunamadı");

        verify(subscriptionRepository, never()).save(any());
        verify(eventProducer, never()).sendPaymentRequest(any());
    }

    @Test
    @DisplayName("Should get subscription by id successfully")
    void testGetSubscriptionById_Success() {
        // Given
        when(subscriptionRepository.findById(anyLong()))
                .thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toResponse(any(Subscription.class)))
                .thenReturn(subscriptionResponse);

        // When
        SubscriptionResponse result = subscriptionService.getSubscriptionById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(subscriptionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when subscription not found")
    void testGetSubscriptionById_NotFound() {
        // Given
        when(subscriptionRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.getSubscriptionById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Subscription not found");
    }

    @Test
    @DisplayName("Should get all subscriptions successfully")
    void testGetAllSubscriptions_Success() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(subscription);
        List<SubscriptionResponse> responses = Arrays.asList(subscriptionResponse);

        when(subscriptionRepository.findAll()).thenReturn(subscriptions);
        when(subscriptionMapper.toResponseList(anyList())).thenReturn(responses);

        // When
        List<SubscriptionResponse> result = subscriptionService.getAllSubscriptions();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(subscriptionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update subscription status successfully")
    void testUpdateStatus_Success() {
        // Given
        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setStatus(SubscriptionStatus.FROZEN);

        when(subscriptionRepository.findById(anyLong()))
                .thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(subscription);
        when(subscriptionMapper.toResponse(any(Subscription.class)))
                .thenReturn(subscriptionResponse);

        // When
        SubscriptionResponse result = subscriptionService.updateStatus(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should delete subscription successfully")
    void testDeleteSubscription_Success() {
        // Given
        when(subscriptionRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(subscriptionRepository).deleteById(anyLong());

        // When
        subscriptionService.deleteSubscription(1L);

        // Then
        verify(subscriptionRepository, times(1)).existsById(1L);
        verify(subscriptionRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent subscription")
    void testDeleteSubscription_NotFound() {
        // Given
        when(subscriptionRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> subscriptionService.deleteSubscription(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Subscription not found");

        verify(subscriptionRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should update subscription status from payment success event")
    void testUpdateSubscriptionStatusFromEvent_PaymentSuccess() {
        // Given
        PaymentStatusEvent statusEvent = PaymentStatusEvent.builder()
                .subscriptionId(1L)
                .status(PaymentStatus.PAYMENT_SUCCESS)
                .transactionId("TXN123")
                .build();

        when(subscriptionRepository.findById(anyLong()))
                .thenReturn(Optional.of(subscription));
        when(offerService.findById(anyLong())).thenReturn(offer);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(subscription);
        when(customerServiceClient.getCustomerById(anyLong()))
                .thenReturn(ResponseEntity.ok(dtoCustomer));

        // When
        subscriptionService.updateSubscriptionStatusFromEvent(statusEvent);

        // Then
        verify(subscriptionRepository, times(1)).findById(1L);
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));

        // KafkaTemplate'in topic ile birlikte çağrıldığını verify et
        verify(kafkaTemplate, times(1)).send(eq("notification-topic"), any());
    }

    @Test
    @DisplayName("Should update subscription status from payment failed event")
    void testUpdateSubscriptionStatusFromEvent_PaymentFailed() {
        // Given
        PaymentStatusEvent statusEvent = PaymentStatusEvent.builder()
                .subscriptionId(1L)
                .status(PaymentStatus.PAYMENT_FAILED)
                .transactionId("TXN123")
                .build();

        when(subscriptionRepository.findById(anyLong()))
                .thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(subscription);
        when(customerServiceClient.getCustomerById(anyLong()))
                .thenReturn(ResponseEntity.ok(dtoCustomer));

        // When
        subscriptionService.updateSubscriptionStatusFromEvent(statusEvent);

        // Then
        ArgumentCaptor<Subscription> subscriptionCaptor =
                ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue().getStatus())
                .isEqualTo(SubscriptionStatus.CANCELLED);

        // Kafka notification verify
        verify(kafkaTemplate, times(1)).send(eq("notification-topic"), any());
    }

    @Test
    @DisplayName("Should handle non-existent subscription in payment event")
    void testUpdateSubscriptionStatusFromEvent_SubscriptionNotFound() {
        // Given
        PaymentStatusEvent statusEvent = PaymentStatusEvent.builder()
                .subscriptionId(999L)
                .status(PaymentStatus.PAYMENT_SUCCESS)
                .transactionId("TXN123")
                .build();

        when(subscriptionRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // When
        subscriptionService.updateSubscriptionStatusFromEvent(statusEvent);

        // Then
        verify(subscriptionRepository, times(1)).findById(999L);
        verify(subscriptionRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }
}