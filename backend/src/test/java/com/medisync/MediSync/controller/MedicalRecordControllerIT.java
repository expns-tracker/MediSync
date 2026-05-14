package com.medisync.MediSync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medisync.MediSync.dto.MedicalRecordCreateDto;
import com.medisync.MediSync.entity.*;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.entity.enums.Gender;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.repository.AppointmentRepository;
import com.medisync.MediSync.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MedicalRecordControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private MedicalRecord testMedicalRecord;
    private Appointment testAppointment;
    private MedicalRecordCreateDto updateDto;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        medicalRecordRepository.deleteAll();
        appointmentRepository.deleteAll();

        // Create test patient
        User patientUser = User.builder()
                .email("patient@example.com")
                .password("password123")
                .role(Role.PATIENT)
                .isActive(true)
                .build();

        Patient patient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .user(patientUser)
                .build();

        // Create test doctor
        User doctorUser = User.builder()
                .email("doctor@example.com")
                .password("password123")
                .role(Role.DOCTOR)
                .isActive(true)
                .build();

        Doctor doctor = Doctor.builder()
                .firstName("Dr.")
                .lastName("Smith")
                .specialization(Specialization.CARDIOLOGY)
                .appointmentDuration(AppointmentDuration.MINUTES_30)
                .user(doctorUser)
                .build();

        // Create test appointment
        testAppointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();
        testAppointment = appointmentRepository.save(testAppointment);

        // Create test medical record
        testMedicalRecord = MedicalRecord.builder()
                .appointment(testAppointment)
                .diagnosis("Hypertension")
                .treatmentPlan("Medication and lifestyle changes")
                .prescription("Lisinopril 10mg daily")
                .build();
        testMedicalRecord = medicalRecordRepository.save(testMedicalRecord);

        updateDto = MedicalRecordCreateDto.builder()
                .diagnosis("Updated Hypertension")
                .treatmentPlan("Updated treatment plan")
                .prescription("Updated prescription")
                .build();
    }

    // SCENARIO 1: Success Flow (Update)
    @Test
    @WithMockUser(roles = "DOCTOR")
    void updateMedicalRecord_Success() throws Exception {
        mockMvc.perform(put("/api/medical-records/{medicalRecordId}", testMedicalRecord.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis", is("Updated Hypertension")))
                .andExpect(jsonPath("$.treatmentPlan", is("Updated treatment plan")))
                .andExpect(jsonPath("$.prescription", is("Updated prescription")));

        // Verify medical record was updated in database
        MedicalRecord updatedRecord = medicalRecordRepository.findById(testMedicalRecord.getId()).orElseThrow();
        assertThat(updatedRecord.getDiagnosis()).isEqualTo("Updated Hypertension");
        assertThat(updatedRecord.getTreatmentPlan()).isEqualTo("Updated treatment plan");
        assertThat(updatedRecord.getPrescription()).isEqualTo("Updated prescription");
    }

    // SCENARIO 2: Delete Flow
    @Test
    @WithMockUser(roles = "DOCTOR")
    void deleteMedicalRecord_Success() throws Exception {
        mockMvc.perform(delete("/api/medical-records/{medicalRecordId}", testMedicalRecord.getId()))
                .andExpect(status().isNoContent());

        // Verify medical record was deleted and appointment status was reset
        assertThat(medicalRecordRepository.existsById(testMedicalRecord.getId())).isFalse();
        Appointment updatedAppointment = appointmentRepository.findById(testAppointment.getId()).orElseThrow();
        assertThat(updatedAppointment.getMedicalRecord()).isNull();
        assertThat(updatedAppointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    // SCENARIO 3: Error/Validation Flow
    @Test
    @WithMockUser(roles = "DOCTOR")
    void updateMedicalRecord_ValidationFail_MissingDiagnosis() throws Exception {
        MedicalRecordCreateDto invalidDto = MedicalRecordCreateDto.builder()
                .treatmentPlan("Treatment plan")
                .prescription("Prescription")
                .build();

        mockMvc.perform(put("/api/medical-records/{medicalRecordId}", testMedicalRecord.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void updateMedicalRecord_NotFound() throws Exception {
        mockMvc.perform(put("/api/medical-records/{medicalRecordId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void deleteMedicalRecord_NotFound() throws Exception {
        mockMvc.perform(delete("/api/medical-records/{medicalRecordId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMedicalRecord_ForbiddenNoDoctorRole() throws Exception {
        mockMvc.perform(put("/api/medical-records/{medicalRecordId}", testMedicalRecord.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteMedicalRecord_ForbiddenNoDoctorRole() throws Exception {
        mockMvc.perform(delete("/api/medical-records/{medicalRecordId}", testMedicalRecord.getId()))
                .andExpect(status().isForbidden());
    }
}
