package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.DoctorScheduleCreateDto;
import com.medisync.MediSync.dto.DoctorScheduleDto;
import com.medisync.MediSync.entity.Doctor;
import com.medisync.MediSync.entity.DoctorSchedule;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.DoctorRepository;
import com.medisync.MediSync.repository.DoctorScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceTest {

    @Mock private DoctorScheduleRepository doctorScheduleRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorScheduleService doctorScheduleService;

    private Doctor doctor;
    private DoctorSchedule schedule;
    private DoctorScheduleCreateDto createDto;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .email("dr.smith@example.com")
                .role(Role.DOCTOR)
                .isActive(true)
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .specialization(Specialization.CARDIOLOGY)
                .appointmentDuration(AppointmentDuration.THIRTY_MINUTES)
                .user(user)
                .build();

        schedule = DoctorSchedule.builder()
                .id(1L)
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        createDto = DoctorScheduleCreateDto.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();
    }

    @Test
    void getSchedules_Success() {
        // Given
        List<DoctorSchedule> schedules = List.of(schedule);
        when(doctorScheduleRepository.findByDoctorId(1L)).thenReturn(schedules);

        // When
        List<DoctorScheduleDto> result = doctorScheduleService.getSchedules(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        verify(doctorScheduleRepository).findByDoctorId(1L);
    }

    @Test
    void getSchedules_Empty() {
        // Given
        when(doctorScheduleRepository.findByDoctorId(1L)).thenReturn(List.of());

        // When
        List<DoctorScheduleDto> result = doctorScheduleService.getSchedules(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(doctorScheduleRepository).findByDoctorId(1L);
    }

    @Test
    void getSchedule_Success() {
        // Given
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        // When
        DoctorScheduleDto result = doctorScheduleService.getSchedule(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        verify(doctorScheduleRepository).findById(1L);
    }

    @Test
    void getSchedule_NotFound() {
        // Given
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorScheduleService.getSchedule(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule with id 1 not found");

        verify(doctorScheduleRepository).findById(1L);
    }

    @Test
    void createSchedule_Success() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.existsByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY)).thenReturn(false);
        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenReturn(schedule);

        // When
        DoctorScheduleDto result = doctorScheduleService.createSchedule(1L, createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        verify(doctorRepository).findById(1L);
        verify(doctorScheduleRepository).existsByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY);
        verify(doctorScheduleRepository).save(any(DoctorSchedule.class));
    }

    @Test
    void createSchedule_DoctorNotFound() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorScheduleService.createSchedule(1L, createDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found");

        verify(doctorRepository).findById(1L);
        verify(doctorScheduleRepository, never()).existsByDoctorIdAndDayOfWeek(anyLong(), any());
    }

    @Test
    void createSchedule_ScheduleAlreadyExists() {
        // Given
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.existsByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> doctorScheduleService.createSchedule(1L, createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Doctor already has a schedule for MONDAY");

        verify(doctorRepository).findById(1L);
        verify(doctorScheduleRepository).existsByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY);
        verify(doctorScheduleRepository, never()).save(any(DoctorSchedule.class));
    }

    @Test
    void updateSchedule_Success() {
        // Given
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenReturn(schedule);

        // When
        DoctorScheduleDto result = doctorScheduleService.updateSchedule(1L, createDto);

        // Then
        assertThat(result).isNotNull();
        verify(doctorScheduleRepository).findById(1L);
        verify(doctorScheduleRepository).save(schedule);
    }

    @Test
    void updateSchedule_NotFound() {
        // Given
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorScheduleService.updateSchedule(1L, createDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule not found");

        verify(doctorScheduleRepository).findById(1L);
        verify(doctorScheduleRepository, never()).save(any(DoctorSchedule.class));
    }

    @Test
    void deleteSchedule_Success() {
        // Given
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        // When
        doctorScheduleService.deleteSchedule(1L, 1L);

        // Then
        verify(doctorScheduleRepository).findById(1L);
        verify(doctorScheduleRepository).deleteById(1L);
    }

    @Test
    void deleteSchedule_NotFound() {
        // Given
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorScheduleService.deleteSchedule(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule with id 1 not found");

        verify(doctorScheduleRepository).findById(1L);
        verify(doctorScheduleRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteSchedule_WrongDoctor() {
        // Given
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        // When & Then
        assertThatThrownBy(() -> doctorScheduleService.deleteSchedule(1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Schedule does  not belong to doctor");

        verify(doctorScheduleRepository).findById(1L);
        verify(doctorScheduleRepository, never()).deleteById(anyLong());
    }
}
