package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.DepartmentCreateDto;
import com.medisync.MediSync.dto.DepartmentDto;
import com.medisync.MediSync.dto.DepartmentUpdateDto;
import com.medisync.MediSync.entity.Department;
import com.medisync.MediSync.entity.Doctor;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.DepartmentRepository;
import com.medisync.MediSync.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;
    private Doctor doctor;
    private DepartmentCreateDto createDto;
    private DepartmentUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Cardiology")
                .description("Heart and cardiovascular diseases")
                .build();

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
                .appointmentDuration(AppointmentDuration.MINUTES_30)
                .user(user)
                .department(department)
                .build();

        createDto = DepartmentCreateDto.builder()
                .name("Neurology")
                .description("Brain and nervous system disorders")
                .build();

        updateDto = DepartmentUpdateDto.builder()
                .name("Updated Cardiology")
                .description("Updated heart and cardiovascular diseases")
                .departmentHeadId(1L)
                .build();
    }

    @Test
    void getAllDepartments_Success() {
        // Given
        List<Department> departments = List.of(department);
        when(departmentRepository.findAll()).thenReturn(departments);

        // When
        List<DepartmentDto> result = departmentService.getAllDepartments();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Cardiology");
        assertThat(result.get(0).getDescription()).isEqualTo("Heart and cardiovascular diseases");
        verify(departmentRepository).findAll();
    }

    @Test
    void getAllDepartments_Empty() {
        // Given
        when(departmentRepository.findAll()).thenReturn(List.of());

        // When
        List<DepartmentDto> result = departmentService.getAllDepartments();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(departmentRepository).findAll();
    }

    @Test
    void getDepartmentById_Success() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        // When
        DepartmentDto result = departmentService.getDepartmentById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Cardiology");
        assertThat(result.getDescription()).isEqualTo("Heart and cardiovascular diseases");
        verify(departmentRepository).findById(1L);
    }

    @Test
    void getDepartmentById_NotFound() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> departmentService.getDepartmentById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department with id=1not found.");

        verify(departmentRepository).findById(1L);
    }

    @Test
    void createDepartment_Success() {
        // Given
        when(departmentRepository.existsByName(createDto.getName())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentDto result = departmentService.createDepartment(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Cardiology");
        assertThat(result.getDescription()).isEqualTo("Heart and cardiovascular diseases");
        verify(departmentRepository).existsByName(createDto.getName());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_NameAlreadyExists() {
        // Given
        when(departmentRepository.existsByName(createDto.getName())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> departmentService.createDepartment(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Department with name " + createDto.getName() + " already exists.");

        verify(departmentRepository).existsByName(createDto.getName());
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void updateDepartment_Success() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName(department.getName())).thenReturn(false);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentDto result = departmentService.updateDepartment(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(departmentRepository).findById(1L);
        verify(doctorRepository).findById(1L);
        verify(departmentRepository).save(department);
    }

    @Test
    void updateDepartment_Success_NoDepartmentHead() {
        // Given
        DepartmentUpdateDto dtoNoHead = DepartmentUpdateDto.builder()
                .name("Updated Cardiology")
                .description("Updated description")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName(department.getName())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentDto result = departmentService.updateDepartment(1L, dtoNoHead);

        // Then
        assertThat(result).isNotNull();
        verify(departmentRepository).findById(1L);
        verify(doctorRepository, never()).findById(any());
        verify(departmentRepository).save(department);
    }

    @Test
    void updateDepartment_DepartmentNotFound() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> departmentService.updateDepartment(1L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department with id=1 not found.");

        verify(departmentRepository).findById(1L);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void updateDepartment_NameAlreadyExists() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName(department.getName())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> departmentService.updateDepartment(1L, updateDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Department with name " + department.getName() + " already exists.");

        verify(departmentRepository).findById(1L);
        verify(departmentRepository).existsByName(department.getName());
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void updateDepartment_DoctorNotFound() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName(department.getName())).thenReturn(false);
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> departmentService.updateDepartment(1L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor with id=1 not found.");

        verify(departmentRepository).findById(1L);
        verify(doctorRepository).findById(1L);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void updateDepartment_DoctorNotInDepartment() {
        // Given
        Department otherDepartment = Department.builder()
                .id(2L)
                .name("Neurology")
                .build();
        doctor.setDepartment(otherDepartment);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName(department.getName())).thenReturn(false);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // When & Then
        assertThatThrownBy(() -> departmentService.updateDepartment(1L, updateDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Doctor with id=" + doctor.getId() + " does not belong to this department.");

        verify(departmentRepository).findById(1L);
        verify(doctorRepository).findById(1L);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void deleteDepartment_Success() {
        // Given
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(doctorRepository.existsByDepartmentId(1L)).thenReturn(false);

        // When
        departmentService.deleteDepartment(1L);

        // Then
        verify(departmentRepository).existsById(1L);
        verify(doctorRepository).existsByDepartmentId(1L);
        verify(departmentRepository).deleteById(1L);
    }

    @Test
    void deleteDepartment_NotFound() {
        // Given
        when(departmentRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> departmentService.deleteDepartment(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department with id=1 not found.");

        verify(departmentRepository).existsById(1L);
        verify(doctorRepository, never()).existsByDepartmentId(any());
        verify(departmentRepository, never()).deleteById(any());
    }

    @Test
    void deleteDepartment_HasAssociatedDoctors() {
        // Given
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(doctorRepository.existsByDepartmentId(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> departmentService.deleteDepartment(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Can't delete the department since it has doctors associated with it");

        verify(departmentRepository).existsById(1L);
        verify(doctorRepository).existsByDepartmentId(1L);
        verify(departmentRepository, never()).deleteById(any());
    }
}
