package com.yigitusq.notification_service.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String email; // mail adresi veya telefon numarası
    private String subject; // "Hoş Geldiniz" veya "Aboneliğiniz Aktif"
    private String message; // Gönderilecek mesaj içeriği
}
