package com.yigitusq.subscription_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // Gerekli import
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Gerekli import
import com.yigitusq.subscription_service.controller.SubscriptionController;
import com.yigitusq.subscription_service.dto.CreateSubscriptionRequest;
import com.yigitusq.subscription_service.dto.SubscriptionResponse;
import com.yigitusq.subscription_service.dto.UpdateStatusRequest;
import com.yigitusq.subscription_service.model.SubscriptionStatus;
import com.yigitusq.subscription_service.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith; // Gerekli import
import org.mockito.InjectMocks; // Gerekli import
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension; // Gerekli import
// import org.springframework.beans.factory.annotation.Autowired; // Artık Spring context'i yok
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // KALDIRILDI
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders; // Gerekli import
import static org.assertj.core.api.Assertions.assertThat;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

// @WebMvcTest(SubscriptionController.class) // <-- KALDIRILDI. Spring context'i yüklemeyeceğiz.
@ExtendWith(MockitoExtension.class) // <-- Mockito'yu etkinleştirmek için EKLENDİ.
@DisplayName("Subscription Controller Tests")
class SubscriptionControllerTest {

    // @Autowired // <-- Artık Spring tarafından enjekte edilmiyor.
    private MockMvc mockMvc;

    // @Autowired // <-- Artık Spring tarafından enjekte edilmiyor.
    private ObjectMapper objectMapper;

    @Mock // <-- Bu sahte (mock) nesne
    private SubscriptionService subscriptionService;

    @InjectMocks // <-- Mockito'ya subscriptionService'i bu controller'a enjekte etmesini söyler
    private SubscriptionController subscriptionController;

    private SubscriptionResponse subscriptionResponse;
    private CreateSubscriptionRequest createRequest;

    @BeforeEach
    void setUp() {
        // MockMvc'yi Spring context olmadan, manuel olarak kuruyoruz.
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
                // .setControllerAdvice(new GlobalExceptionHandler()) // Gerekirse exception handler'ınızı buraya ekleyebilirsiniz
                .build();

        // ObjectMapper'ı manuel olarak oluşturuyoruz (LocalDateTime'ı işlemesi için)
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


        // --- Test verileriniz aynı kalıyor ---
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
                .andExpect(status().isBadRequest()); // <-- Bu test, standalone modda doğrulama (validation) yapılandırmanıza bağlıdır.

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
                .andExpect(status().isBadRequest()); // <-- Bu test de doğrulama yapılandırmasına bağlıdır.

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
        when(subscriptionService.getSubscriptionById(999L))
                .thenThrow(new RuntimeException("Subscription not found. Id: 999"));

        // When & Then
        // perform() çağrısının kendisinin bir ServletException fırlatmasını bekliyoruz.
        ServletException exception = assertThrows(ServletException.class, () -> {
            // perform() çağrısını bu lambda ifadesinin içine taşıyın
            mockMvc.perform(get("/api/subscriptions/999")
                    .contentType(MediaType.APPLICATION_JSON));
        });

        Throwable rootCause = exception.getCause();

        // 2. Asıl sebebin bizim fırlattığımız RuntimeException olduğunu doğrulayın
        assertNotNull(rootCause, "ServletException'in bir sebebi olmalı (cause)");
        assertTrue(rootCause instanceof RuntimeException, "Asıl sebep RuntimeException olmalı");

        // 3. Hata mesajının beklediğimiz gibi olduğunu doğrulayın
        assertEquals("Subscription not found. Id: 999", rootCause.getMessage());

        // 4. Mock'un yine de çağrıldığını doğrulayın
        verify(subscriptionService, times(1)).getSubscriptionById(999L);
    }
}