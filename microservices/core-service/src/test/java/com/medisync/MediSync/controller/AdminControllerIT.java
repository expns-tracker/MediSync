package com.medisync.MediSync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medisync.MediSync.dto.AdminRegistrationDto;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testAdmin;
    private AdminRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        userRepository.deleteAll();

        testAdmin = User.builder()
                .email("existing.admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        testAdmin = userRepository.save(testAdmin);

        registrationDto = AdminRegistrationDto.builder()
                .email("new.admin@example.com")
                .password("password123")
                .build();
    }

    // SCENARIO 1: Success Flow (Create + Read)
    @Test
    @WithMockUser(roles = "ADMIN")
    void registerAdmin_Success() throws Exception {
        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("new.admin@example.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")));

        // Verify admin was created
        assertThat(userRepository.existsByEmail(registrationDto.getEmail())).isTrue();
        User savedAdmin = userRepository.findByEmail(registrationDto.getEmail()).orElseThrow();
        assertThat(savedAdmin.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAdmins_Success() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].email", is("existing.admin@example.com")))
                .andExpect(jsonPath("$[0].role", is("ADMIN")));
    }

    // SCENARIO 2: Update/Delete Flow (Delete)
    @Test
    @WithMockUser(username = "other.admin@example.com", roles = "ADMIN")
    void deleteAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/admins/{adminId}", testAdmin.getId()))
                .andExpect(status().isNoContent());

        // Verify admin was deleted
        assertThat(userRepository.existsById(testAdmin.getId())).isFalse();
    }

    // SCENARIO 3: Error/Validation Flow
    @Test
    @WithMockUser(roles = "ADMIN")
    void registerAdmin_ValidationFail_MissingEmail() throws Exception {
        AdminRegistrationDto invalidDto = AdminRegistrationDto.builder()
                .password("password123")
                .build();

        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerAdmin_ValidationFail_EmailAlreadyExists() throws Exception {
        // First register an admin
        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated());

        // Try to register again with same email
        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "existing.admin@example.com", roles = "ADMIN")
    void deleteAdmin_CannotDeleteSelf() throws Exception {
        mockMvc.perform(delete("/api/admins/{adminId}", testAdmin.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAdmin_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admins/{adminId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAdmins_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerAdmin_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAdmin_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(delete("/api/admins/{adminId}", testAdmin.getId()))
                .andExpect(status().isForbidden());
    }
}
