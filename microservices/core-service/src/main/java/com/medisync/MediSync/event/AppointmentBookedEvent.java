package com.medisync.MediSync.event;

import com.medisync.MediSync.dto.NotificationRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppointmentBookedEvent extends ApplicationEvent {
    private final NotificationRequest notificationRequest;

    public AppointmentBookedEvent(Object source, NotificationRequest notificationRequest) {
        super(source);
        this.notificationRequest = notificationRequest;
    }
}
