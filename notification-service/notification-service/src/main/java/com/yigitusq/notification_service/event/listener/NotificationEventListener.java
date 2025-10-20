package com.yigitusq.notification_service.event.listener;

import com.yigitusq.notification_service.event.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper; // YENİ IMPORT
import com.yigitusq.notification_service.event.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    // JSON çevirisi için gerekli olan standart Spring aracı
    private final ObjectMapper objectMapper;

    public NotificationEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // DEĞİŞİKLİK: Artık NotificationEvent değil, String bekliyoruz.
    @KafkaListener(topics = "${app.kafka.topic.notification}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleNotificationEvent(String eventAsString) {
        log.info("Ham bildirim event'i (String) alındı: {}", eventAsString);

        try {
            // Gelen düz metni (JSON String) manuel olarak NotificationEvent nesnesine çeviriyoruz.
            NotificationEvent event = objectMapper.readValue(eventAsString, NotificationEvent.class);

            // Simülasyon çıktısı (eskisi gibi devam)
            System.out.println("==================================================");
            System.out.println("MAIL GÖNDERİLİYOR (Simülasyon)...");
            System.out.println("Alıcı: " + event.getEmail());
            System.out.println("Konu: " + event.getSubject());
            System.out.println("Mesaj: " + event.getMessage());
            System.out.println("==================================================");

            log.info("'{}' adresine bildirim başarıyla gönderildi (simüle edildi).", event.getEmail());

        } catch (Exception e) {
            log.error("Kafka mesajı NotificationEvent nesnesine çevrilirken hata oluştu!", e);
        }
    }
}