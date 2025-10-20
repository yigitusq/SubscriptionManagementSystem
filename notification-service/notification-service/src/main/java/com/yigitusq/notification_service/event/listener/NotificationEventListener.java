package com.yigitusq.notification_service.event.listener;

import com.yigitusq.notification_service.event.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    @KafkaListener(topics = "${app.kafka.topic.notification}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Notification event received: {}", event);

        System.out.println("==================================================");
        System.out.println("MAIL GÖNDERİLİYOR (Simülasyon)...");
        System.out.println("Alıcı: " + event.getEmail());
        System.out.println("Konu: " + event.getSubject());
        System.out.println("Mesaj: " + event.getMessage());
        System.out.println("==================================================");

        log.info("'{}' adresine bildirim başarıyla gönderildi (simüle edildi).", event.getEmail());
    }
}