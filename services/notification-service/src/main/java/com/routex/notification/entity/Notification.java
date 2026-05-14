package com.routex.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_ref", columnList = "reference_id"),
    @Index(name = "idx_notifications_email", columnList = "recipient_email"),
    @Index(name = "idx_notifications_sent_at", columnList = "sent_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(length = 255) private String recipientEmail;
    @Column(length = 20) private String recipientPhone;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationChannel channel;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationType type;
    @Column(length = 255) private String subject;
    @Column(length = 2000) private String body;
    @Column(length = 255) private String referenceId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;
    private Instant sentAt;
    private String failureReason;
    @Builder.Default private Instant createdAt = Instant.now();
}
