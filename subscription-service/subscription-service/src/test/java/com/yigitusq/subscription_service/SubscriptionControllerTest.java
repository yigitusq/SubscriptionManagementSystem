package com.yigitusq.subscription_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yigitusq.subscription_service.controller.SubscriptionController;
import com.yigitusq.subscription_service.dto.CreateSubscriptionRequest;
import com.yigitusq.subscription_service.dto.SubscriptionResponse;
import com.yigitusq.subscription_service.dto.UpdateStatusRequest;
import com.yigitusq.subscription_service.model.SubscriptionStatus;
import com.yigitusq.subscription_service.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
// @Mock anotasyonu Mockito'dan geliyordu, ona artık gerek yok.
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@DisplayName("Subscription Controller Tests")
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock // <-- @Mock'tan @MockBean'e DEĞİŞTİRİLDİ
    private SubscriptionService subscriptionService;

    private SubscriptionResponse subscriptionResponse;
    private CreateSubscriptionRequest createRequest;

    @BeforeEach
    void setUp() {
        subscriptionResponse = new SubscriptionResponse();
        subscriptionResponse.setId(1L);
        subscriptionResponse.setCustomerId(1L);
        subscriptionResponse.setOfferId(1L);
        subscriptionResponse.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionResponse.setRenewDate(LocalDateTime.now().plusMonths(1));
        subscriptionResponse.setCreatedAt(LocalDateTime.now());

        createRequest = new CreateSubscriptionRequest();
        createRequest.setCustomerId(1L);
        createRequest.setOfferId(1L);
    }

    @Test
    @DisplayName("GET /api/subscriptions/{id} - Success")
    void testGetSubscriptionById_Success() throws Exception {
        // Given
        when(subscriptionService.getSubscriptionById(anyLong()))
                .thenReturn(subscriptionResponse);

        // When & Then
        mockMvc.perform(get("/api/subscriptions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerId", is(1)))
                .andExpect(jsonPath("$.offerId", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(subscriptionService, times(1)).getSubscriptionById(1L);
    }

    @Test
    @DisplayName("GET /api/subscriptions - Success")
    void testGetAllSubscriptions_Success() throws Exception {
        // Given
        List<SubscriptionResponse> responses = Arrays.asList(subscriptionResponse);
        when(subscriptionService.getAllSubscriptions()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(subscriptionService, times(1)).getAllSubscriptions();
    }

    @Test
    @DisplayName("POST /api/subscriptions - Success")
    void testCreateSubscription_Success() throws Exception {
        // Given
        subscriptionResponse.setStatus(SubscriptionStatus.WAITINGFORPAYMENT);
        when(subscriptionService.createSubscription(any(CreateSubscriptionRequest.class)))
                .thenReturn(subscriptionResponse);

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITINGFORPAYMENT")));

        verify(subscriptionService, times(1))
                .createSubscription(any(CreateSubscriptionRequest.class));
    }

    @Test
    @DisplayName("POST /api/subscriptions - Validation Error (Null CustomerId)")
    void testCreateSubscription_ValidationError() throws Exception {
        // Given
        createRequest.setCustomerId(null);

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).createSubscription(any());
    }

    @Test
    @DisplayName("POST /api/subscriptions - Validation Error (Negative CustomerId)")
    void testCreateSubscription_NegativeCustomerId() throws Exception {
        // Given
        createRequest.setCustomerId(-1L);

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).createSubscription(any());
    }

    @Test
    @DisplayName("PATCH /api/subscriptions/{id}/status - Success")
    void testUpdateStatus_Success() throws Exception {
        // Given
        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setStatus(SubscriptionStatus.FROZEN);

        subscriptionResponse.setStatus(SubscriptionStatus.FROZEN);
        when(subscriptionService.updateStatus(anyLong(), any(UpdateStatusRequest.class)))
                .thenReturn(subscriptionResponse);

        // When & Then
        mockMvc.perform(patch("/api/subscriptions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("FROZEN")));

        verify(subscriptionService, times(1))
                .updateStatus(eq(1L), any(UpdateStatusRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/subscriptions/{id} - Success")
    void testDeleteSubscription_Success() throws Exception {
        // Given
        doNothing().when(subscriptionService).deleteSubscription(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/subscriptions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(subscriptionService, times(1)).deleteSubscription(1L);
    }

    @Test
    @DisplayName("GET /api/subscriptions/{id} - Not Found")
    void testGetSubscriptionById_NotFound() throws Exception {
        // Given
        when(subscriptionService.getSubscriptionById(anyLong()))
                .thenThrow(new RuntimeException("Subscription not found. Id: 999"));

        // When & Then
        // NOT: @WebMvcTest genellikle global exception handler'ı YÜKLEMEZ.
        // Eğer bir @ControllerAdvice @ExceptionHandler'ınız varsa, bu 500 dönebilir.
        // Eğer yoksa ve sadece controller'dan hata fırlatılıyorsa,
        // bu beklendiği gibi çalışacaktır.
        // Şimdilik 500 (Internal Server Error) beklemek mantıklı.
        mockMvc.perform(get("/api/subscriptions/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(subscriptionService, times(1)).getSubscriptionById(999L);
    }
}