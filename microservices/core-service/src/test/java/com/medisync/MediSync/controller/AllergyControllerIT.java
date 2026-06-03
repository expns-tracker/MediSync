package com.medisync.MediSync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medisync.MediSync.dto.AllergyCreateDto;
import com.medisync.MediSync.entity.Allergy;
import com.medisync.MediSync.entity.enums.AllergyCategory;
import com.medisync.MediSync.repository.AllergyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for Allergy Controller - E2E Scenarios
 * Three scenarios:
 * 1. Success Flow (Create + Read)
 * 2. Update/Delete Flow
 * 3. Error/Validation Flow
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AllergyControllerIT {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired private AllergyRepository allergyRepository;

    private Long allergyId;

    @BeforeEach
    void setUp() {
        // Create initial test allergy
        Allergy allergy = allergyRepository.save(
                Allergy.builder()
                        .name("Aspirin")
                        .code("ASP")
                        .category(AllergyCategory.MEDICATION)
                        .build()
        );
        allergyId = allergy.getId();
    }

    // ========== SCENARIO 1: Success Flow (Create + Read) ==========

    @Test
    @DisplayName("SCENARIO 1.1: GET /api/allergies - Retrieve All Allergies - Success")
    void getAllAllergies_Success() throws Exception {
        // Pre-arrange: Add another allergy
        allergyRepository.save(
                Allergy.builder()
                        .name("Peanuts")
                        .code("PNT")
                        .category(AllergyCategory.FOOD)
                        .build()
        );

        mockMvc.perform(get("/api/allergies")
                        .with(user("user@test.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Aspirin"))
                .andExpect(jsonPath("$[0].code").value("ASP"))
                .andExpect(jsonPath("$[0].category").value("MEDICATION"));
    }

    @Test
    @DisplayName("SCENARIO 1.2: POST /api/allergies - Create New Allergy - Success")
    void createAllergy_Success() throws Exception {
        AllergyCreateDto createDto = new AllergyCreateDto();
        createDto.setName("Ibuprofen");
        createDto.setCode("IBU");
        createDto.setCategory("MEDICATION");

        mockMvc.perform(post("/api/allergies")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ibuprofen"))
                .andExpect(jsonPath("$.code").value("IBU"))
                .andExpect(jsonPath("$.category").value("MEDICATION"));
    }

    @Test
    @DisplayName("SCENARIO 1.3: GET /api/allergies/{id} - Retrieve Single Allergy - Success")
    void getAllergy_Success() throws Exception {
        mockMvc.perform(get("/api/allergies/{id}", allergyId)
                        .with(user("user@test.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(allergyId))
                .andExpect(jsonPath("$.name").value("Aspirin"))
                .andExpect(jsonPath("$.code").value("ASP"))
                .andExpect(jsonPath("$.category").value("MEDICATION"));
    }

    // ========== SCENARIO 2: Update/Delete Flow ==========

    @Test
    @DisplayName("SCENARIO 2.1: PUT /api/allergies/{id} - Update Allergy - Success")
    void updateAllergy_Success() throws Exception {
        AllergyCreateDto updateDto = new AllergyCreateDto();
        updateDto.setName("Aspirin Updated");
        updateDto.setCode("ASP_U");
        updateDto.setCategory("FOOD");

        mockMvc.perform(put("/api/allergies/{id}", allergyId)
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(allergyId))
                .andExpect(jsonPath("$.name").value("Aspirin Updated"))
                .andExpect(jsonPath("$.code").value("ASP_U"))
                .andExpect(jsonPath("$.category").value("FOOD"));
    }

    @Test
    @DisplayName("SCENARIO 2.2: DELETE /api/allergies/{id} - Delete Allergy - Success")
    void deleteAllergy_Success() throws Exception {
        mockMvc.perform(delete("/api/allergies/{id}", allergyId)
                        .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isOk());

        // Verify deletion by attempting to retrieve it
        mockMvc.perform(get("/api/allergies/{id}", allergyId)
                        .with(user("user@test.com").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("SCENARIO 2.3: PUT /api/allergies/{id} - Update Non-Existent Allergy - 404")
    void updateAllergy_NotFound() throws Exception {
        AllergyCreateDto updateDto = new AllergyCreateDto();
        updateDto.setName("Non-existent");
        updateDto.setCode("NEX");
        updateDto.setCategory("MEDICATION");

        mockMvc.perform(put("/api/allergies/{id}", 9999L)
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    // ========== SCENARIO 3: Error/Validation Flow ==========

    @Test
    @DisplayName("SCENARIO 3.1: POST /api/allergies - Create with Missing Name - 400 Bad Request")
    void createAllergy_ValidationFail_MissingName() throws Exception {
        AllergyCreateDto createDto = new AllergyCreateDto();
        createDto.setName(""); // Empty name
        createDto.setCode("CODE");
        createDto.setCategory("MEDICATION");

        mockMvc.perform(post("/api/allergies")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SCENARIO 3.2: POST /api/allergies - Create with Invalid Category - 400 Bad Request")
    void createAllergy_ValidationFail_InvalidCategory() throws Exception {
        AllergyCreateDto createDto = new AllergyCreateDto();
        createDto.setName("Test Allergy");
        createDto.setCode("TST");
        createDto.setCategory("INVALID_CATEGORY");

        mockMvc.perform(post("/api/allergies")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SCENARIO 3.3: GET /api/allergies/{id} - Get Non-Existent Allergy - 404 Not Found")
    void getAllergy_NotFound() throws Exception {
        mockMvc.perform(get("/api/allergies/{id}", 9999L)
                        .with(user("user@test.com").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("SCENARIO 3.4: POST /api/allergies - Create with Missing Code - 400 Bad Request")
    void createAllergy_ValidationFail_MissingCode() throws Exception {
        AllergyCreateDto createDto = new AllergyCreateDto();
        createDto.setName("Test Allergy");
        createDto.setCode(""); // Empty code
        createDto.setCategory("MEDICATION");

        mockMvc.perform(post("/api/allergies")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SCENARIO 3.5: POST /api/allergies - Create without Admin Role - 403 Forbidden")
    void createAllergy_ForbiddenNoAdminRole() throws Exception {
        AllergyCreateDto createDto = new AllergyCreateDto();
        createDto.setName("Test Allergy");
        createDto.setCode("TST");
        createDto.setCategory("MEDICATION");

        mockMvc.perform(post("/api/allergies")
                        .with(user("user@test.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SCENARIO 3.6: DELETE /api/allergies/{id} - Delete Non-Existent Allergy - 404 Not Found")
    void deleteAllergy_NotFound() throws Exception {
        mockMvc.perform(delete("/api/allergies/{id}", 9999L)
                        .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("SCENARIO 3.7: PUT /api/allergies - Update without Admin Role - 403 Forbidden")
    void updateAllergy_ForbiddenNoAdminRole() throws Exception {
        AllergyCreateDto updateDto = new AllergyCreateDto();
        updateDto.setName("Updated");
        updateDto.setCode("UPD");
        updateDto.setCategory("MEDICATION");

        mockMvc.perform(put("/api/allergies/{id}", allergyId)
                        .with(user("user@test.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SCENARIO 3.8: DELETE /api/allergies/{id} - Delete without Admin Role - 403 Forbidden")
    void deleteAllergy_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(delete("/api/allergies/{id}", allergyId)
                        .with(user("user@test.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SCENARIO 3.9: POST /api/allergies - Create with All Valid Categories")
    void createAllergy_AllCategories() throws Exception {
        String[] categories = {"MEDICATION", "FOOD", "ENVIRONMENTAL", "ANIMAL", "INSECT", "OTHER"};

        for (int i = 0; i < categories.length; i++) {
            AllergyCreateDto createDto = new AllergyCreateDto();
            createDto.setName("Allergy_" + i);
            createDto.setCode("CODE_" + i);
            createDto.setCategory(categories[i]);

            mockMvc.perform(post("/api/allergies")
                            .with(user("admin@test.com").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.category").value(categories[i]));
        }
    }
}





