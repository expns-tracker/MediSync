package com.medisync.MediSync.dto;

import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.validation.ValueOfEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorRegistrationDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Specialization is required")
    @ValueOfEnum(enumClass = Specialization.class, message = "Specialization must be one of the following: " +
            "GENERAL_PRACTICE, " +
            "CARDIOLOGY, " +
            "PEDIATRICS, " +
            "ORTHOPEDICS, " +
            "DERMATOLOGY, " +
            "NEUROLOGY, " +
            "GYNECOLOGY, " +
            "PSYCHIATRY, " +
            "ONCOLOGY, " +
            "RADIOLOGY, " +
            "OPHTHALMOLOGY, " +
            "DENTISTRY, " +
            "SURGERY, " +
            "UROLOGY, " +
            "ENT")
    private String specialization;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Appointment duration is required")
    @ValueOfEnum(enumClass = AppointmentDuration.class, message = "Appointment duration must be one of the following: " +
            "MINUTES_15, " +
            "MINUTES_30, " +
            "MINUTES_45, " +
            "MINUTES_60")
    private String appointmentDuration;

}
