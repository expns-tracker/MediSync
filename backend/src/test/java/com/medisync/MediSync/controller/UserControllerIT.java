package com.medisync.MediSync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medisync.MediSync.dto.ChangePasswordDto;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private ChangePasswordDto changePasswordDto;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test.user@example.com")
                .password(passwordEncoder.encode("currentPass123"))
                .role(Role.PATIENT)
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        changePasswordDto = ChangePasswordDto.builder()
                .currentPassword("currentPass123")
                .newPassword("newPass456")
                .build();
    }

    // SCENARIO 1: Success Flow (Get User + Change Password)
    @Test
    void getUserById_Success() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is("test.user@example.com")))
                .andExpect(jsonPath("$.role", is("PATIENT")));
    }

    @Test
    @WithMockUser(username = "test.user@example.com")
    void changePassword_Success() throws Exception {
        mockMvc.perform(patch("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isOk());

        // Verify password was changed in database
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPass456", updatedUser.getPassword())).isTrue();
    }

    // SCENARIO 2: Error Flow (Not Found + Validation)
    @Test
    void getUserById_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test.user@example.com")
    void changePassword_IncorrectCurrentPassword() throws Exception {
        ChangePasswordDto wrongPasswordDto = ChangePasswordDto.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPass456")
                .build();

        mockMvc.perform(patch("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_Unauthenticated() throws Exception {
        mockMvc.perform(patch("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test.user@example.com")
    void changePassword_ValidationFail_MissingCurrentPassword() throws Exception {
        ChangePasswordDto invalidDto = ChangePasswordDto.builder()
                .newPassword("newPass456")
                .build();

        mockMvc.perform(patch("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test.user@example.com")
    void changePassword_ValidationFail_MissingNewPassword() throws Exception {
        ChangePasswordDto invalidDto = ChangePasswordDto.builder()
                .currentPassword("currentPass123")
                .build();

        mockMvc.perform(patch("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test.user@example.com")
    void changePassword_ValidationFail_WeakNewPassword() throws Exception {
        ChangePasswordDto invalidDto = ChangePasswordDto.builder()
                .currentPassword("currentPass123")
                .newPassword("123")  // Too short
                .build();

        mockMvc.perform(patch("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
