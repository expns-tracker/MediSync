package com.medisync.MediSync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.MediSync.dto.PatientDto;
import com.medisync.MediSync.dto.PatientRegistrationDto;
import com.medisync.MediSync.dto.PatientUpdateDto;
import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.Gender;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.repository.PatientRepository;
import com.medisync.MediSync.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PatientControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    private PatientRegistrationDto registrationDto;
    private PatientUpdateDto updateDto;
    private Patient testPatient;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        patientRepository.deleteAll();
        userRepository.deleteAll();

        registrationDto = PatientRegistrationDto.builder()
                .email("test.patient@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("Patient")
                .gender("male")
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Test St")
                .city("Test City")
                .county("Test County")
                .country("Test Country")
                .allergyIds(null)
                .build();

        updateDto = PatientUpdateDto.builder()
                .firstName("Updated")
                .lastName("Patient")
                .gender(Gender.FEMALE)
                .phoneNumber("0987654321")
                .dateOfBirth(LocalDate.of(1992, 2, 2))
                .address("456 Updated St")
                .city("Updated City")
                .county("Updated County")
                .country("Updated Country")
                .allergyIds(null)
                .build();

        testUser = User.builder()
                .email("existing.patient@example.com")
                .password("password123")
                .role(Role.PATIENT)
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        testPatient = Patient.builder()
                .firstName("Existing")
                .lastName("Patient")
                .gender(Gender.MALE)
                .phoneNumber("1111111111")
                .dateOfBirth(LocalDate.of(1985, 5, 5))
                .address("789 Existing St")
                .city("Existing City")
                .county("Existing County")
                .country("Existing Country")
                .user(testUser)
                .build();
        testPatient = patientRepository.save(testPatient);
    }

    // SCENARIO 1: Success Flow (Create + Read)
    @Test
    void registerPatient_Success() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Patient Registration Successful"));

        // Verify patient was created
        assertThat(userRepository.existsByEmail(registrationDto.getEmail())).isTrue();
        Patient savedPatient = patientRepository.findByUserEmail(registrationDto.getEmail()).orElseThrow();
        assertThat(savedPatient.getFirstName()).isEqualTo("Test");
        assertThat(savedPatient.getLastName()).isEqualTo("Patient");
    }

    @Test
    void getPatientById_Success() throws Exception {
        mockMvc.perform(get("/api/patients/{patientId}", testPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPatient.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("Existing")))
                .andExpect(jsonPath("$.lastName", is("Patient")))
                .andExpect(jsonPath("$.email", is("existing.patient@example.com")));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void getPatients_Success() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .param("search", "Existing")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].firstName", is("Existing")));
    }

    // SCENARIO 2: Update/Delete Flow
    @Test
    @WithMockUser(username = "existing.patient@example.com", roles = "PATIENT")
    void updatePatient_Success() throws Exception {
        mockMvc.perform(put("/api/patients/{patientId}", testPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("Patient")))
                .andExpect(jsonPath("$.phoneNumber", is("0987654321")));

        // Verify patient was updated in database
        Patient updatedPatient = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertThat(updatedPatient.getFirstName()).isEqualTo("Updated");
        assertThat(updatedPatient.getPhoneNumber()).isEqualTo("0987654321");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivatePatient_Success() throws Exception {
        mockMvc.perform(put("/api/patients/{patientId}/deactivate", testPatient.getId()))
                .andExpect(status().isNoContent());

        // Verify patient was deactivated
        User deactivatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(deactivatedUser.getIsActive()).isFalse();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activatePatient_Success() throws Exception {
        // First deactivate the patient
        testUser.setIsActive(false);
        userRepository.save(testUser);

        mockMvc.perform(put("/api/patients/{patientId}/activate", testPatient.getId()))
                .andExpect(status().isNoContent());

        // Verify patient was activated
        User activatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(activatedUser.getIsActive()).isTrue();
    }

    // SCENARIO 3: Error/Validation Flow
    @Test
    void registerPatient_ValidationFail_MissingEmail() throws Exception {
        PatientRegistrationDto invalidDto = PatientRegistrationDto.builder()
                .password("password123")
                .firstName("Test")
                .lastName("Patient")
                .gender("male")
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Test St")
                .city("Test City")
                .county("Test County")
                .country("Test Country")
                .build();

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerPatient_ValidationFail_EmailAlreadyExists() throws Exception {
        // First register a patient
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated());

        // Try to register again with same email
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPatientById_NotFound() throws Exception {
        mockMvc.perform(get("/api/patients/{patientId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPatients_ForbiddenNoRole() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "wrong.user@example.com", roles = "PATIENT")
    void updatePatient_ForbiddenWrongUser() throws Exception {
        mockMvc.perform(put("/api/patients/{patientId}", testPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePatient_ForbiddenNoRole() throws Exception {
        mockMvc.perform(put("/api/patients/{patientId}", testPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deactivatePatient_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(put("/api/patients/{patientId}/deactivate", testPatient.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void activatePatient_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(put("/api/patients/{patientId}/activate", testPatient.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivatePatient_NotFound() throws Exception {
        mockMvc.perform(put("/api/patients/{patientId}/deactivate", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activatePatient_NotFound() throws Exception {
        mockMvc.perform(put("/api/patients/{patientId}/activate", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "existing.patient@example.com", roles = "PATIENT")
    void getAppointmentsByPatientId_Success() throws Exception {
        mockMvc.perform(get("/api/patients/{patientId}/appointments", testPatient.getId())
                        .param("timeframe", "all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(java.util.List.class)));
    }

    @Test
    void getAppointmentsByPatientId_Forbidden() throws Exception {
        mockMvc.perform(get("/api/patients/{patientId}/appointments", testPatient.getId()))
                .andExpect(status().isForbidden());
    }
}
