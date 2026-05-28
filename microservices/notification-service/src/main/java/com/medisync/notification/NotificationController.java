package com.medisync.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    @PostMapping("/send")
    public void sendNotification(@RequestBody String message) {
        log.info("Received notification request: {}", message);
        // Simulate sending email/SMS
        System.out.println("NOTIF: " + message);
    }
}
