package com.medisync.MediSync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.MediSync.dto.DoctorDto;
import com.medisync.MediSync.dto.DoctorRegistrationDto;
import com.medisync.MediSync.dto.DoctorUpdateDto;
import com.medisync.MediSync.entity.Department;
import com.medisync.MediSync.entity.Doctor;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.repository.DepartmentRepository;
import com.medisync.MediSync.repository.DoctorRepository;
import com.medisync.MediSync.repository.UserRepository;
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
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DoctorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private DoctorRegistrationDto registrationDto;
    private DoctorUpdateDto updateDto;
    private Doctor testDoctor;
    private User testUser;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        doctorRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        // Create test department
        testDepartment = Department.builder()
                .name("Cardiology")
                .build();
        testDepartment = departmentRepository.save(testDepartment);

        registrationDto = DoctorRegistrationDto.builder()
                .email("dr.test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("Doctor")
                .specialization("cardiology")
                .appointmentDuration("thirty_minutes")
                .departmentId(testDepartment.getId())
                .build();

        updateDto = DoctorUpdateDto.builder()
                .firstName("Updated")
                .lastName("Doctor")
                .specialization("neurology")
                .appointmentDuration("sixty_minutes")
                .departmentId(testDepartment.getId())
                .build();

        testUser = User.builder()
                .email("dr.existing@example.com")
                .password("password123")
                .role(Role.DOCTOR)
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        testDoctor = Doctor.builder()
                .firstName("Existing")
                .lastName("Doctor")
                .specialization(Specialization.CARDIOLOGY)
                .appointmentDuration(AppointmentDuration.THIRTY_MINUTES)
                .user(testUser)
                .department(testDepartment)
                .build();
        testDoctor = doctorRepository.save(testDoctor);
    }

    // SCENARIO 1: Success Flow (Create + Read)
    @Test
    @WithMockUser(roles = "ADMIN")
    void registerDoctor_Success() throws Exception {
        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("Doctor")))
                .andExpect(jsonPath("$.specialization", is("CARDIOLOGY")));

        // Verify doctor was created
        assertThat(userRepository.existsByEmail(registrationDto.getEmail())).isTrue();
        Doctor savedDoctor = doctorRepository.findByUserEmail(registrationDto.getEmail()).orElseThrow();
        assertThat(savedDoctor.getFirstName()).isEqualTo("Test");
        assertThat(savedDoctor.getSpecialization()).isEqualTo(Specialization.CARDIOLOGY);
    }

    @Test
    void getDoctorById_Success() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}", testDoctor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testDoctor.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("Existing")))
                .andExpect(jsonPath("$.lastName", is("Doctor")))
                .andExpect(jsonPath("$.specialization", is("CARDIOLOGY")));
    }

    @Test
    void getDoctors_Success() throws Exception {
        mockMvc.perform(get("/api/doctors")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].firstName", is("Existing")));
    }

    @Test
    void getAvailableSlots_Success() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/appointments/slots", testDoctor.getId())
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)));
    }

    // SCENARIO 2: Update/Delete Flow
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDoctor_Success() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}", testDoctor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("Doctor")))
                .andExpect(jsonPath("$.specialization", is("NEUROLOGY")));

        // Verify doctor was updated in database
        Doctor updatedDoctor = doctorRepository.findById(testDoctor.getId()).orElseThrow();
        assertThat(updatedDoctor.getFirstName()).isEqualTo("Updated");
        assertThat(updatedDoctor.getSpecialization()).isEqualTo(Specialization.NEUROLOGY);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateDoctor_Success() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}/deactivate", testDoctor.getId()))
                .andExpect(status().isNoContent());

        // Verify doctor was deactivated
        User deactivatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(deactivatedUser.getIsActive()).isFalse();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateDoctor_Success() throws Exception {
        // First deactivate the doctor
        testUser.setIsActive(false);
        userRepository.save(testUser);

        mockMvc.perform(put("/api/doctors/{doctorId}/activate", testDoctor.getId()))
                .andExpect(status().isNoContent());

        // Verify doctor was activated
        User activatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(activatedUser.getIsActive()).isTrue();
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void getAppointmentsByDoctorId_Success() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/appointments", testDoctor.getId())
                        .param("timeframe", "all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(List.class)));
    }

    // SCENARIO 3: Error/Validation Flow
    @Test
    @WithMockUser(roles = "ADMIN")
    void registerDoctor_ValidationFail_MissingEmail() throws Exception {
        DoctorRegistrationDto invalidDto = DoctorRegistrationDto.builder()
                .password("password123")
                .firstName("Test")
                .lastName("Doctor")
                .specialization("cardiology")
                .appointmentDuration("thirty_minutes")
                .departmentId(testDepartment.getId())
                .build();

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerDoctor_ValidationFail_EmailAlreadyExists() throws Exception {
        // First register a doctor
        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated());

        // Try to register again with same email
        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDoctorById_NotFound() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDoctor_NotFound() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateDoctor_NotFound() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}/deactivate", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateDoctor_NotFound() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}/activate", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerDoctor_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDoctor_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}", testDoctor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deactivateDoctor_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}/deactivate", testDoctor.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void activateDoctor_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}/activate", testDoctor.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAppointmentsByDoctorId_ForbiddenNoRole() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/appointments", testDoctor.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAvailableSlots_DoctorNotFound() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/appointments/slots", 999L)
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAvailableSlots_InvalidDate() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/appointments/slots", testDoctor.getId())
                        .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());
    }
}
