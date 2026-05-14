package com.medisync.MediSync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.MediSync.dto.CredentialsDto;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CredentialsDto validCredentials;
    private CredentialsDto invalidCredentials;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user and patient
        User testUser = User.builder()
                .email("test.patient@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.PATIENT)
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        Patient testPatient = Patient.builder()
                .firstName("Test")
                .lastName("Patient")
                .gender(Gender.MALE)
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Test St")
                .city("Test City")
                .county("Test County")
                .country("Test Country")
                .user(testUser)
                .build();
        patientRepository.save(testPatient);

        validCredentials = CredentialsDto.builder()
                .email("test.patient@example.com")
                .password("password123")
                .build();

        invalidCredentials = CredentialsDto.builder()
                .email("wrong@example.com")
                .password("wrongpassword")
                .build();
    }

    // SCENARIO 1: Success Flow (Login)
    @Test
    void login_Success() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCredentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.token", not(emptyString())));
    }

    // SCENARIO 2: Error Flow (Invalid Credentials)
    @Test
    void login_InvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCredentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_ValidationFail_MissingEmail() throws Exception {
        CredentialsDto invalidDto = CredentialsDto.builder()
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidationFail_MissingPassword() throws Exception {
        CredentialsDto invalidDto = CredentialsDto.builder()
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidationFail_InvalidEmailFormat() throws Exception {
        CredentialsDto invalidDto = CredentialsDto.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
