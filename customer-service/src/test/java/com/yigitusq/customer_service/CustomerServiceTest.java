package com.yigitusq.customer_service;

import com.yigitusq.customer_service.dto.DtoCustomer;
import com.yigitusq.customer_service.dto.DtoCustomerIU;
import com.yigitusq.customer_service.event.dto.NotificationEvent;
import com.yigitusq.customer_service.mapper.CustomerMapper;
import com.yigitusq.customer_service.model.Customer;
import com.yigitusq.customer_service.repository.CustomerRepository;
import com.yigitusq.customer_service.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils; // YENİ IMPORT

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList; // anyString yerine anyList
import static org.mockito.ArgumentMatchers.eq; // YENİ IMPORT
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomerMapper customerMapper;
    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private DtoCustomer dtoCustomer;
    private DtoCustomerIU dtoCustomerIU;

    @BeforeEach
    void setUp() {
        // YENİ EKLENDİ: @Value ile alınan 'notificationTopic' alanını test için manuel olarak doldur
        ReflectionTestUtils.setField(customerService, "notificationTopic", "test-notification-topic");

        customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPassword("hashedPassword");
        customer.setStatus("ACTIVE");
        customer.setMobile("5551234567");

        dtoCustomer = new DtoCustomer();
        dtoCustomer.setId(1L);
        dtoCustomer.setName("John");
        dtoCustomer.setSurname("Doe");
        dtoCustomer.setEmail("john.doe@example.com");
        dtoCustomer.setStatus("ACTIVE");

        dtoCustomerIU = new DtoCustomerIU();
        dtoCustomerIU.setName("John");
        dtoCustomerIU.setSurname("Doe");
        dtoCustomerIU.setEmail("john.doe@example.com");
        dtoCustomerIU.setPassword("password123");
        dtoCustomerIU.setStatus("ACTIVE");
        dtoCustomerIU.setMobile("5551234567");
    }

    @Test
    @DisplayName("Should find customer by id successfully")
    void testFindById_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toDto(any(Customer.class))).thenReturn(dtoCustomer);
        // When
        DtoCustomer result = customerService.findById(1L);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void testFindById_NotFound() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> customerService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Customer not found - id: 999");
    }

    @Test
    @DisplayName("Should find all customers successfully")
    void testFindAll_Success() {
        // Given
        List<Customer> customers = Arrays.asList(customer);
        List<DtoCustomer> dtoCustomers = Arrays.asList(dtoCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.toDtoList(anyList())).thenReturn(dtoCustomers);
        // When
        List<DtoCustomer> result = customerService.findAll();
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should save customer successfully")
    void testSave_Success() {
        // Given
        when(customerMapper.toEntity(any(DtoCustomerIU.class))).thenReturn(customer);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(dtoCustomer);

        // When
        DtoCustomer result = customerService.save(dtoCustomerIU);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        // Verify password was encoded
        verify(passwordEncoder, times(1)).encode("password123");
        // Verify customer was saved
        verify(customerRepository, times(1)).save(any(Customer.class));

        // DÜZELTİLDİ: Her iki Kafka topic'ini de DOĞRU İSİMLERLE doğrula
        // 1. "customer-events" topic'ini doğrula
        verify(kafkaTemplate, times(1)).send(eq("customer-events"), any(Customer.class));

        // 2. "notification-topic"i doğrula
        ArgumentCaptor<NotificationEvent> notificationCaptor =
                ArgumentCaptor.forClass(NotificationEvent.class);
        verify(kafkaTemplate, times(1)).send(eq("test-notification-topic"), notificationCaptor.capture());

        NotificationEvent sentNotification = notificationCaptor.getValue();
        assertThat(sentNotification.getTo()).isEqualTo("john.doe@example.com");
        assertThat(sentNotification.getSubject()).contains("Hoş Geldiniz");
    }

    @Test
    @DisplayName("Should delete customer (soft delete)")
    void testDeleteById_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        // When
        customerService.deleteById(1L);
        // Then
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository, times(1)).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertThat(savedCustomer.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent customer")
    void testDeleteById_NotFound() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> customerService.deleteById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Customer not found - id: 999");
        verify(customerRepository, never()).save(any());
    }
}