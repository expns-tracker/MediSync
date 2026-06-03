package com.medisync.MediSync.dto;


import com.medisync.MediSync.entity.MedicalRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MedicalRecordDto {
    private Long id;
    private String diagnosis;
    private String treatmentPlan;
    private String prescription;
    private Long appointmentId;
    private LocalDateTime appointmentTime;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MedicalRecordDto mapToDto(MedicalRecord medicalRecord) {
        return MedicalRecordDto.builder()
                .id(medicalRecord.getId())
                .diagnosis(medicalRecord.getDiagnosis())
                .treatmentPlan(medicalRecord.getTreatmentPlan())
                .prescription(medicalRecord.getPrescription())
                .appointmentId(medicalRecord.getAppointment().getId())
                .doctorId(medicalRecord.getAppointment().getDoctor().getId())
                .doctorName(
                        medicalRecord.getAppointment().getDoctor().getFirstName()
                                + " " + medicalRecord.getAppointment().getDoctor().getLastName()
                )
                .patientId(medicalRecord.getAppointment().getPatient().getId())
                .patientName(
                        medicalRecord.getAppointment().getPatient().getFirstName()
                                + " " + medicalRecord.getAppointment().getPatient().getLastName()
                )
                .createdAt(medicalRecord.getCreatedAt())
                .updatedAt(medicalRecord.getUpdatedAt())
                .build();
    }
}
