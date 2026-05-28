package com.medisync.notification.service;

import com.medisync.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(NotificationRequest request) {
        log.info("Preparing to send email to {}", request.getTo());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@medisync.com");
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());

            mailSender.send(message);
            log.info("Email sent successfully to {}", request.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}. Reason: {}", request.getTo(), e.getMessage());
        }
    }
}
