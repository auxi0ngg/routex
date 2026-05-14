package com.routex.notification.repository;
import com.routex.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByReferenceIdOrderBySentAtDesc(String referenceId);
}
