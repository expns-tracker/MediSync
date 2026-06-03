package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.PatientDto;
import com.medisync.MediSync.dto.PatientRegistrationDto;
import com.medisync.MediSync.dto.PatientUpdateDto;
import com.medisync.MediSync.entity.Allergy;
import com.medisync.MediSync.entity.Appointment;
import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.entity.enums.Gender;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.AllergyRepository;
import com.medisync.MediSync.repository.AppointmentRepository;
import com.medisync.MediSync.repository.PatientRepository;
import com.medisync.MediSync.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private AllergyRepository allergyRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private User user;
    private PatientRegistrationDto registrationDto;
    private PatientUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("patient@example.com")
                .password("encodedPassword")
                .role(Role.PATIENT)
                .isActive(true)
                .build();

        patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .user(user)
                .allergies(new ArrayList<>())
                .build();

        registrationDto = PatientRegistrationDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("patient@example.com")
                .password("password123")
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .allergyIds(List.of(1L, 2L))
                .build();

        updateDto = PatientUpdateDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .dateOfBirth(LocalDate.of(1992, 2, 2))
                .gender(Gender.FEMALE)
                .allergyIds(List.of(3L))
                .build();
    }

    // Tests for registerPatient

    @Test
    void registerPatient_Success() {
        when(userRepository.existsByEmail("patient@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(allergyRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(new Allergy(), new Allergy()));

        patientService.registerPatient(registrationDto);

        verify(userRepository, times(1)).existsByEmail("patient@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(allergyRepository, times(1)).findAllById(List.of(1L, 2L));
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void registerPatient_EmailExists() {
        when(userRepository.existsByEmail("patient@example.com")).thenReturn(true);

        assertThatThrownBy(() -> patientService.registerPatient(registrationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("There is already an account associated with this email: patient@example.com");

        verify(userRepository, times(1)).existsByEmail("patient@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void registerPatient_NoAllergies() {
        PatientRegistrationDto dtoNoAllergies = PatientRegistrationDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("patient@example.com")
                .password("password123")
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .build();

        when(userRepository.existsByEmail("patient@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        patientService.registerPatient(dtoNoAllergies);

        verify(userRepository, times(1)).existsByEmail("patient@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(allergyRepository, never()).findAllById(any());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    // Tests for getById

    @Test
    void getById_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientDto result = patientService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("patient@example.com");
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    void getById_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient with id=99 not found!");
        verify(patientRepository, times(1)).findById(99L);
    }

    // Tests for searchPatients

    @Test
    void searchPatients_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> patientPage = new PageImpl<>(List.of(patient), pageable, 1);

        when(patientRepository.findActivePatientsWithSearch("John", pageable)).thenReturn(patientPage);

        Page<PatientDto> result = patientService.searchPatients("John", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
        verify(patientRepository, times(1)).findActivePatientsWithSearch("John", pageable);
    }

    @Test
    void searchPatients_EmptySearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> patientPage = new PageImpl<>(List.of(patient), pageable, 1);

        when(patientRepository.findActivePatientsWithSearch(null, pageable)).thenReturn(patientPage);

        Page<PatientDto> result = patientService.searchPatients("", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(patientRepository, times(1)).findActivePatientsWithSearch(null, pageable);
    }

    // Tests for updatePatient

    @Test
    void updatePatient_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.existsByEmail("patient@example.com")).thenReturn(true);
        when(allergyRepository.findAllById(List.of(3L))).thenReturn(List.of(new Allergy()));

        PatientDto result = patientService.updatePatient(1L, updateDto, "patient@example.com");

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getPhoneNumber()).isEqualTo("0987654321");
        verify(patientRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("patient@example.com");
        verify(allergyRepository, times(1)).findAllById(List.of(3L));
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void updatePatient_PatientNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(99L, updateDto, "patient@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient with id=99 not found!");
        verify(patientRepository, times(1)).findById(99L);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void updatePatient_UserNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.existsByEmail("wrong@example.com")).thenReturn(false);

        assertThatThrownBy(() -> patientService.updatePatient(1L, updateDto, "wrong@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("There is no account associated with this email: wrong@example.com");
        verify(patientRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("wrong@example.com");
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void updatePatient_Unauthorized() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> patientService.updatePatient(1L, updateDto, "other@example.com"))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("You are not authorized to access perform this action.");
        verify(patientRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("other@example.com");
        verify(patientRepository, never()).save(any(Patient.class));
    }

    // Tests for deactivatePatient

    @Test
    void deactivatePatient_Success() {
        Appointment appointment = Appointment.builder()
                .id(1L)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findAllByPatientIdAndStatus(1L, AppointmentStatus.SCHEDULED))
                .thenReturn(List.of(appointment));

        patientService.deactivatePatient(1L);

        verify(patientRepository, times(1)).findById(1L);
        verify(appointmentRepository, times(1)).findAllByPatientIdAndStatus(1L, AppointmentStatus.SCHEDULED);
        verify(appointmentRepository, times(1)).saveAll(List.of(appointment));
        verify(userRepository, times(1)).save(any(User.class));
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(user.getIsActive()).isFalse();
    }

    @Test
    void deactivatePatient_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.deactivatePatient(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient with id=99 not found!");
        verify(patientRepository, times(1)).findById(99L);
        verify(appointmentRepository, never()).findAllByPatientIdAndStatus(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivatePatient_AlreadyInactive() {
        user.setIsActive(false);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.deactivatePatient(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User is already not active for patient with id=1");
        verify(patientRepository, times(1)).findById(1L);
        verify(appointmentRepository, never()).findAllByPatientIdAndStatus(any(), any());
        verify(userRepository, never()).save(any());
    }

    // Tests for activatePatient

    @Test
    void activatePatient_Success() {
        user.setIsActive(false);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        patientService.activatePatient(1L);

        verify(patientRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    void activatePatient_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.activatePatient(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient with id=99 not found!");
        verify(patientRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void activatePatient_AlreadyActive() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.activatePatient(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User is already active for patient with id=1");
        verify(patientRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }
}
