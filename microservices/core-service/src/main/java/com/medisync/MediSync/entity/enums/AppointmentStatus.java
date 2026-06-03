package com.medisync.MediSync.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppointmentStatus {
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NO_SHOW("Patient didn't show");

    private final String label;
}
