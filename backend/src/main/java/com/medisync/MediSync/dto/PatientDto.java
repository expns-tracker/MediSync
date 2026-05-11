package com.medisync.MediSync.dto;

import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PatientDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String city;
    private String county;
    private String country;
    private List<Long> allergyIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PatientDto mapToDto(Patient patient) {
        return PatientDto.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .email(patient.getUser().getEmail())
                .phoneNumber(patient.getPhoneNumber())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .city(patient.getCity())
                .county(patient.getCounty())
                .country(patient.getCountry())
                .allergyIds(patient.getAllergies() != null ? patient.getAllergies().stream().map(allergy -> allergy.getId()).collect(Collectors.toList()) : null)
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
