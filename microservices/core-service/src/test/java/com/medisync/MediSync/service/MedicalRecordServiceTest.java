package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.MedicalRecordCreateDto;
import com.medisync.MediSync.dto.MedicalRecordDto;
import com.medisync.MediSync.entity.*;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.entity.enums.Gender;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.AppointmentRepository;
import com.medisync.MediSync.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private MedicalRecord medicalRecord;
    private Appointment appointment;
    private MedicalRecordCreateDto updateDto;

    @BeforeEach
    void setUp() {
        // Create test patient
        User patientUser = User.builder()
                .id(1L)
                .email("patient@example.com")
                .role(Role.PATIENT)
                .isActive(true)
                .build();

        Patient patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .user(patientUser)
                .build();

        // Create test doctor
        User doctorUser = User.builder()
                .id(2L)
                .email("doctor@example.com")
                .role(Role.DOCTOR)
                .isActive(true)
                .build();

        Doctor doctor = Doctor.builder()
                .id(1L)
                .firstName("Dr.")
                .lastName("Smith")
                .specialization(Specialization.CARDIOLOGY)
                .appointmentDuration(AppointmentDuration.MINUTES_30)
                .user(doctorUser)
                .build();

        // Create test appointment
        appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();

        // Create test medical record
        medicalRecord = MedicalRecord.builder()
                .id(1L)
                .appointment(appointment)
                .diagnosis("Hypertension")
                .treatmentPlan("Medication and lifestyle changes")
                .prescription("Lisinopril 10mg daily")
                .build();

        appointment.setMedicalRecord(medicalRecord);

        updateDto = MedicalRecordCreateDto.builder()
                .diagnosis("Updated Hypertension")
                .treatmentPlan("Updated treatment plan")
                .prescription("Updated prescription")
                .build();
    }

    @Test
    void deleteMedicalRecord_Success() {
        // Given
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));

        // When
        medicalRecordService.deleteMedicalRecord(1L);

        // Then
        assertThat(appointment.getMedicalRecord()).isNull();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(medicalRecordRepository).findById(1L);
        verify(medicalRecordRepository).deleteById(1L);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void deleteMedicalRecord_NotFound() {
        // Given
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> medicalRecordService.deleteMedicalRecord(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Medical Record Not Found");

        verify(medicalRecordRepository).findById(1L);
        verify(medicalRecordRepository, never()).deleteById(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void updateMedicalRecord_Success() {
        // Given
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        // When
        MedicalRecordDto result = medicalRecordService.updateMedicalRecord(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiagnosis()).isEqualTo("Updated Hypertension");
        assertThat(result.getTreatmentPlan()).isEqualTo("Updated treatment plan");
        assertThat(result.getPrescription()).isEqualTo("Updated prescription");
        verify(medicalRecordRepository).findById(1L);
        verify(medicalRecordRepository).save(medicalRecord);
    }

    @Test
    void updateMedicalRecord_NotFound() {
        // Given
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> medicalRecordService.updateMedicalRecord(1L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Medical Record Not Found");

        verify(medicalRecordRepository).findById(1L);
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }
}
