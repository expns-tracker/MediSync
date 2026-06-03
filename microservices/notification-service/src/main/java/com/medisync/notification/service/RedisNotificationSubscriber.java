package com.medisync.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("Received message from Redis Queue");
        try {
            String jsonPayload = new String(message.getBody());
            NotificationRequest request = objectMapper.readValue(jsonPayload, NotificationRequest.class);
            emailService.sendEmail(request);
        } catch (Exception e) {
            log.error("Failed to process message from Redis Queue: {}", e.getMessage());
        }
    }
}
