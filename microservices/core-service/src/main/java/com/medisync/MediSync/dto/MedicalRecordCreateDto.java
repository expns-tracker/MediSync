package com.medisync.MediSync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordCreateDto {

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 255, message = "Diagnosis must be under 255 characters")
    private String diagnosis;

    private String treatmentPlan;

    private String prescription;
}