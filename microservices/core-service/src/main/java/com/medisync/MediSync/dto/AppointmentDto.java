package com.medisync.MediSync.dto;

import com.medisync.MediSync.entity.Appointment;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentDto {
    private Long id;
    private LocalDateTime appointmentTime;
    private String reason;
    private AppointmentStatus status;
    private DoctorDto doctor;
    private PatientDto patient;
    
    // Flattened fields for UI robustness
    private String patientFirstName;
    private String patientLastName;
    private Long patientId;
    private String doctorFirstName;
    private String doctorLastName;
    private Long doctorId;
    private String departmentName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MedicalRecordDto medicalRecord;

    public static AppointmentDto mapToDto(Appointment appointment) {
        return AppointmentDto.builder()
                .id(appointment.getId())
                .appointmentTime(appointment.getAppointmentTime())
                .reason(appointment.getReason())
                .status(appointment.getStatus())
                .doctor(DoctorDto.mapToDto(appointment.getDoctor()))
                .patient(PatientDto.mapToDto(appointment.getPatient()))
                .patientFirstName(appointment.getPatient().getFirstName())
                .patientLastName(appointment.getPatient().getLastName())
                .patientId(appointment.getPatient().getId())
                .doctorFirstName(appointment.getDoctor().getFirstName())
                .doctorLastName(appointment.getDoctor().getLastName())
                .doctorId(appointment.getDoctor().getId())
                .departmentName(appointment.getDoctor().getDepartment().getName())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .medicalRecord(
                        appointment.getMedicalRecord() != null ?
                        MedicalRecordDto.mapToDto(appointment.getMedicalRecord()) :
                        null
                )
                .build();
    }
}
