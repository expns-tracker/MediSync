package com.medisync.MediSync.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppointmentDuration {
    MINUTES_15(15),
    MINUTES_30(30),
    MINUTES_45(45),
    MINUTES_60(60);

    private final int minutes;
}
