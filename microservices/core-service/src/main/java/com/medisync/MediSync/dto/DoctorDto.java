package com.medisync.MediSync.dto;

import com.medisync.MediSync.entity.Doctor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DoctorDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String specializationLabel;
    private Long departmentId;
    private String departmentName;
    private Integer appointmentDuration;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DoctorDto mapToDto(Doctor doctor) {
        return DoctorDto.builder()
                .id(doctor.getId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .specialization(doctor.getSpecialization().name())
                .specializationLabel(doctor.getSpecialization().getLabel())
                .departmentId(doctor.getDepartment().getId())
                .departmentName(doctor.getDepartment().getName())
                .appointmentDuration(doctor.getAppointmentDuration().getMinutes())
                .isActive(doctor.getUser().getIsActive())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }
}
