package com.medisync.MediSync.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentBookDto {

    @NotNull(message = "Patient id is required")
    private Long patientId;

    @NotNull(message = "Doctor id is required")
    private Long doctorId;

    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment time cannot be from the past")
    private LocalDateTime appointmentTime;

    @NotBlank(message = "Reason is required")
    private String reason;
}
