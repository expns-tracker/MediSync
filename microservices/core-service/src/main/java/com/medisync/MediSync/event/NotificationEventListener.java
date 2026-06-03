package com.medisync.MediSync.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String NOTIFICATION_TOPIC = "notificationQueue";

    @Async
    @EventListener
    public void handleAppointmentBookedEvent(AppointmentBookedEvent event) {
        log.info("Async event listener received notification request for: {}", event.getNotificationRequest().getTo());
        try {
            String jsonPayload = objectMapper.writeValueAsString(event.getNotificationRequest());
            redisTemplate.convertAndSend(NOTIFICATION_TOPIC, jsonPayload);
            log.info("Successfully published notification message to Redis Queue.");
        } catch (Exception e) {
            log.error("Failed to publish to Redis Queue: {}", e.getMessage());
        }
    }
}
