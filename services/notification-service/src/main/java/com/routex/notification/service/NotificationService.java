package com.routex.notification.service;
import com.routex.notification.entity.Notification;
import java.util.*;
public interface NotificationService {
    void sendShipmentNotification(String trackingNumber, String status, String recipientEmail, String recipientPhone);
    void sendDriverAssignmentNotification(UUID driverId, String shipmentTrackingNumber);
    void sendEtaNotification(String trackingNumber, long etaMinutes, String recipientEmail);
    List<Notification> getNotificationsByReference(String referenceId);
}
