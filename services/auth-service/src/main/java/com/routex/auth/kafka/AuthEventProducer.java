package com.routex.auth.kafka;

import com.routex.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_EVENTS_TOPIC = "user-events";

    public void publishUserRegistered(User user) {
        Map<String, Object> event = Map.of(
            "eventType", "USER_REGISTERED",
            "userId", user.getId().toString(),
            "email", user.getEmail(),
            "role", user.getRole().name(),
            "organizationId", user.getOrganizationId() != null ? user.getOrganizationId().toString() : "",
            "timestamp", Instant.now().toString()
        );
        sendEvent(USER_EVENTS_TOPIC, user.getId().toString(), event);
    }

    public void publishUserLoggedIn(User user) {
        Map<String, Object> event = Map.of(
            "eventType", "USER_LOGGED_IN",
            "userId", user.getId().toString(),
            "email", user.getEmail(),
            "timestamp", Instant.now().toString()
        );
        sendEvent(USER_EVENTS_TOPIC, user.getId().toString(), event);
    }

    private void sendEvent(String topic, String key, Object payload) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send event to topic {}: {}", topic, ex.getMessage());
            } else {
                log.debug("Event sent to topic {} partition {} offset {}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });
    }
}
