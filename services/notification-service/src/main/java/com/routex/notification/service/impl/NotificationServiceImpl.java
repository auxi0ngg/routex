package com.routex.notification.service.impl;

import com.routex.notification.entity.*;
import com.routex.notification.repository.NotificationRepository;
import com.routex.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public void sendShipmentNotification(String trackingNumber, String status,
                                          String recipientEmail, String recipientPhone) {
        String subject = "Shipment Update — " + trackingNumber;
        String body = buildShipmentMessage(trackingNumber, status);

        // Email channel
        sendEmail(recipientEmail, subject, body);

        // Persist notification record
        Notification notification = Notification.builder()
            .recipientEmail(recipientEmail)
            .recipientPhone(recipientPhone)
            .channel(NotificationChannel.EMAIL)
            .type(NotificationType.SHIPMENT_UPDATE)
            .subject(subject)
            .body(body)
            .referenceId(trackingNumber)
            .status(NotificationStatus.SENT)
            .sentAt(Instant.now())
            .build();
        notificationRepository.save(notification);

        log.info("Notification sent for shipment {} status {}", trackingNumber, status);
    }

    @Override
    @Transactional
    public void sendDriverAssignmentNotification(UUID driverId, String shipmentTrackingNumber) {
        String body = "You have been assigned a new delivery: " + shipmentTrackingNumber
                    + ". Please check your app for details.";
        Notification notification = Notification.builder()
            .channel(NotificationChannel.PUSH)
            .type(NotificationType.DRIVER_ASSIGNMENT)
            .subject("New Delivery Assignment")
            .body(body)
            .referenceId(driverId.toString())
            .status(NotificationStatus.PENDING)
            .build();
        notificationRepository.save(notification);
        // In production: send via FCM/APNs to driver's device token
        log.info("Driver assignment notification queued for driver: {}", driverId);
    }

    @Override
    @Transactional
    public void sendEtaNotification(String trackingNumber, long etaMinutes, String recipientEmail) {
        String subject = "ETA Update — " + trackingNumber;
        String body = "Your shipment " + trackingNumber + " will arrive in approximately "
                    + etaMinutes + " minutes.";
        sendEmail(recipientEmail, subject, body);

        Notification notification = Notification.builder()
            .recipientEmail(recipientEmail)
            .channel(NotificationChannel.EMAIL)
            .type(NotificationType.ETA_UPDATE)
            .subject(subject)
            .body(body)
            .referenceId(trackingNumber)
            .status(NotificationStatus.SENT)
            .sentAt(Instant.now())
            .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByReference(String referenceId) {
        return notificationRepository.findByReferenceIdOrderBySentAtDesc(referenceId);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@routex.io");
            mailSender.send(message);
            log.debug("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildShipmentMessage(String trackingNumber, String status) {
        return switch (status) {
            case "PICKED_UP"       -> "Your shipment " + trackingNumber + " has been picked up and is on its way.";
            case "IN_TRANSIT"      -> "Your shipment " + trackingNumber + " is in transit.";
            case "OUT_FOR_DELIVERY"-> "Your shipment " + trackingNumber + " is out for delivery today!";
            case "DELIVERED"       -> "Your shipment " + trackingNumber + " has been delivered successfully. Thank you for choosing RouteX!";
            case "FAILED"          -> "Delivery attempt for " + trackingNumber + " was unsuccessful. We'll try again.";
            case "RETURNED"        -> "Shipment " + trackingNumber + " is being returned. Please contact support.";
            default                -> "Shipment " + trackingNumber + " status updated to: " + status;
        };
    }
}
