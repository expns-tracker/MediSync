package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.DoctorDto;
import com.medisync.MediSync.dto.DoctorRegistrationDto;
import com.medisync.MediSync.dto.DoctorUpdateDto;
import com.medisync.MediSync.entity.Appointment;
import com.medisync.MediSync.entity.Department;
import com.medisync.MediSync.entity.Doctor;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.AppointmentRepository;
import com.medisync.MediSync.repository.DepartmentRepository;
import com.medisync.MediSync.repository.DoctorRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock private DoctorRepository doctorRepository;
    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;
    private User user;
    private Department department;
    private DoctorRegistrationDto registrationDto;
    private DoctorUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Cardiology")
                .build();

        user = User.builder()
                .id(1L)
                .email("dr.smith@example.com")
                .password("encodedPassword")
                .role(Role.DOCTOR)
                .isActive(true)
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .specialization(Specialization.CARDIOLOGY)
                .appointmentDuration(AppointmentDuration.MINUTES_30)
                .user(user)
                .department(department)
                .build();

        registrationDto = DoctorRegistrationDto.builder()
                .email("dr.smith@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Smith")
                .specialization("cardiology")
                .appointmentDuration("thirty_minutes")
                .departmentId(1L)
                .build();

        updateDto = DoctorUpdateDto.builder()
                .firstName("Jane")
                .lastName("Doe")
                .specialization("neurology")
                .appointmentDuration("sixty_minutes")
                .departmentId(1L)
                .build();
    }

    @Test
    void getDoctorById_Success() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // When
        DoctorDto result = doctorService.getDoctorById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getSpecialization()).isEqualTo(Specialization.CARDIOLOGY);
        verify(doctorRepository).findById(1L);
    }

    @Test
    void getDoctorById_NotFound() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorService.getDoctorById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor with id 1 not found");

        verify(doctorRepository).findById(1L);
    }

    @Test
    void getDoctors_AllActiveDoctors() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Doctor> doctors = List.of(doctor);
        Page<Doctor> doctorPage = new PageImpl<>(doctors, pageable, 1);

        when(doctorRepository.findAllByUserIsActive(true, pageable)).thenReturn(doctorPage);

        // When
        Page<DoctorDto> result = doctorService.getDoctors(null, false, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
        verify(doctorRepository).findAllByUserIsActive(true, pageable);
    }

    @Test
    void getDoctors_ByDepartment() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Doctor> doctors = List.of(doctor);
        Page<Doctor> doctorPage = new PageImpl<>(doctors, pageable, 1);

        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(doctorRepository.findByDepartmentIdAndUserIsActive(1L, true, pageable)).thenReturn(doctorPage);

        // When
        Page<DoctorDto> result = doctorService.getDoctors(1L, false, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(departmentRepository).existsById(1L);
        verify(doctorRepository).findByDepartmentIdAndUserIsActive(1L, true, pageable);
    }

    @Test
    void getDoctors_DeactivatedDoctors() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Doctor> doctors = List.of(doctor);
        Page<Doctor> doctorPage = new PageImpl<>(doctors, pageable, 1);

        when(doctorRepository.findAllByUserIsActive(false, pageable)).thenReturn(doctorPage);

        // When
        Page<DoctorDto> result = doctorService.getDoctors(null, true, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(doctorRepository).findAllByUserIsActive(false, pageable);
    }

    @Test
    void getDoctors_DepartmentNotFound() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(departmentRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> doctorService.getDoctors(999L, false, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department with id 999 not found");

        verify(departmentRepository).existsById(999L);
        verify(doctorRepository, never()).findByDepartmentIdAndUserIsActive(any(), any(), any());
    }

    @Test
    void registerDoctor_Success() {
        // Given
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        // When
        DoctorDto result = doctorService.registerDoctor(registrationDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getSpecialization()).isEqualTo(Specialization.CARDIOLOGY);
        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(passwordEncoder).encode(registrationDto.getPassword());
        verify(userRepository).save(any(User.class));
        verify(departmentRepository).findById(1L);
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void registerDoctor_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> doctorService.registerDoctor(registrationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("There is already an account associated with this email: " + registrationDto.getEmail());

        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void registerDoctor_DepartmentNotFound() {
        // Given
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorService.registerDoctor(registrationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department with id=1 not found");

        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(passwordEncoder).encode(registrationDto.getPassword());
        verify(userRepository).save(any(User.class));
        verify(departmentRepository).findById(1L);
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void updateDoctor_Success() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        // When
        DoctorDto result = doctorService.updateDoctor(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(doctorRepository).findById(1L);
        verify(departmentRepository).findById(1L);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void updateDoctor_DoctorNotFound() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorService.updateDoctor(1L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor with id 1 not found");

        verify(doctorRepository).findById(1L);
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void updateDoctor_DepartmentNotFound() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorService.updateDoctor(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department with id=1 not found");

        verify(doctorRepository).findById(1L);
        verify(departmentRepository).findById(1L);
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void deactivateDoctor_Success() {
        // Given
        Appointment appointment = Appointment.builder()
                .id(1L)
                .status(AppointmentStatus.SCHEDULED)
                .build();
        List<Appointment> scheduledAppointments = List.of(appointment);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findAllByDoctorIdAndStatus(1L, AppointmentStatus.SCHEDULED))
                .thenReturn(scheduledAppointments);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        doctorService.deactivateDoctor(1L);

        // Then
        assertThat(user.getIsActive()).isFalse();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(doctorRepository).findById(1L);
        verify(appointmentRepository).findAllByDoctorIdAndStatus(1L, AppointmentStatus.SCHEDULED);
        verify(appointmentRepository).saveAll(scheduledAppointments);
        verify(userRepository).save(user);
    }

    @Test
    void deactivateDoctor_DoctorNotFound() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorService.deactivateDoctor(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor with id 1 not found");

        verify(doctorRepository).findById(1L);
        verify(appointmentRepository, never()).findAllByDoctorIdAndStatus(any(), any());
    }

    @Test
    void deactivateDoctor_AlreadyInactive() {
        // Given
        user.setIsActive(false);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // When & Then
        assertThatThrownBy(() -> doctorService.deactivateDoctor(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is already not active for doctor with id 1");

        verify(doctorRepository).findById(1L);
        verify(appointmentRepository, never()).findAllByDoctorIdAndStatus(any(), any());
    }

    @Test
    void activateDoctor_Success() {
        // Given
        user.setIsActive(false);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        doctorService.activateDoctor(1L);

        // Then
        assertThat(user.getIsActive()).isTrue();
        verify(doctorRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void activateDoctor_DoctorNotFound() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorService.activateDoctor(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor with id 1 not found");

        verify(doctorRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateDoctor_AlreadyActive() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // When & Then
        assertThatThrownBy(() -> doctorService.activateDoctor(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is already active for doctor with id 1");

        verify(doctorRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
}
