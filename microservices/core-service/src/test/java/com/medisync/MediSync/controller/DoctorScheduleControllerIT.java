package com.medisync.MediSync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medisync.MediSync.dto.DoctorScheduleCreateDto;
import com.medisync.MediSync.entity.*;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.repository.DoctorRepository;
import com.medisync.MediSync.repository.DoctorScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DoctorScheduleControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    private Doctor testDoctor;
    private DoctorSchedule testSchedule;
    private DoctorScheduleCreateDto createDto;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        doctorScheduleRepository.deleteAll();
        doctorRepository.deleteAll();

        // Create test doctor
        User doctorUser = User.builder()
                .email("dr.test@example.com")
                .password("password123")
                .role(Role.DOCTOR)
                .isActive(true)
                .build();

        testDoctor = Doctor.builder()
                .firstName("Test")
                .lastName("Doctor")
                .specialization(Specialization.CARDIOLOGY)
                .appointmentDuration(AppointmentDuration.MINUTES_30)
                .user(doctorUser)
                .build();
        testDoctor = doctorRepository.save(testDoctor);

        // Create test schedule
        testSchedule = DoctorSchedule.builder()
                .doctor(testDoctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();
        testSchedule = doctorScheduleRepository.save(testSchedule);

        createDto = DoctorScheduleCreateDto.builder()
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(16, 0))
                .build();
    }

    // SCENARIO 1: Success Flow (Create + Read)
    @Test
    void getFullSchedule_Success() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/schedules", testDoctor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].dayOfWeek", is("MONDAY")));
    }

    @Test
    void getSchedule_Success() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/schedules/{scheduleId}", testDoctor.getId(), testSchedule.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testSchedule.getId().intValue())))
                .andExpect(jsonPath("$.dayOfWeek", is("MONDAY")))
                .andExpect(jsonPath("$.startTime", is("09:00:00")))
                .andExpect(jsonPath("$.endTime", is("17:00:00")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchedule_Success() throws Exception {
        mockMvc.perform(post("/api/doctors/{doctorId}/schedules", testDoctor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek", is("TUESDAY")))
                .andExpect(jsonPath("$.startTime", is("10:00:00")))
                .andExpect(jsonPath("$.endTime", is("16:00:00")));

        // Verify schedule was created
        DoctorSchedule savedSchedule = doctorScheduleRepository.findByDoctorId(testDoctor.getId())
                .stream()
                .filter(s -> s.getDayOfWeek() == DayOfWeek.TUESDAY)
                .findFirst()
                .orElseThrow();
        assertThat(savedSchedule.getStartTime()).isEqualTo(LocalTime.of(10, 0));
    }

    // SCENARIO 2: Update/Delete Flow
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchedule_Success() throws Exception {
        DoctorScheduleCreateDto updateDto = DoctorScheduleCreateDto.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        mockMvc.perform(put("/api/doctors/{doctorId}/schedules/{scheduleId}", testDoctor.getId(), testSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime", is("08:00:00")))
                .andExpect(jsonPath("$.endTime", is("18:00:00")));

        // Verify schedule was updated in database
        DoctorSchedule updatedSchedule = doctorScheduleRepository.findById(testSchedule.getId()).orElseThrow();
        assertThat(updatedSchedule.getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(updatedSchedule.getEndTime()).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSchedule_Success() throws Exception {
        mockMvc.perform(delete("/api/doctors/{doctorId}/schedules/{scheduleId}", testDoctor.getId(), testSchedule.getId()))
                .andExpect(status().isNoContent());

        // Verify schedule was deleted
        assertThat(doctorScheduleRepository.existsById(testSchedule.getId())).isFalse();
    }

    // SCENARIO 3: Error/Validation Flow
    @Test
    void getFullSchedule_DoctorNotFound() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/schedules", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getSchedule_NotFound() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/schedules/{scheduleId}", testDoctor.getId(), 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchedule_DoctorNotFound() throws Exception {
        mockMvc.perform(post("/api/doctors/{doctorId}/schedules", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchedule_ValidationFail_MissingDayOfWeek() throws Exception {
        DoctorScheduleCreateDto invalidDto = DoctorScheduleCreateDto.builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(16, 0))
                .build();

        mockMvc.perform(post("/api/doctors/{doctorId}/schedules", testDoctor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchedule_Conflict_SameDay() throws Exception {
        DoctorScheduleCreateDto conflictDto = DoctorScheduleCreateDto.builder()
                .dayOfWeek(DayOfWeek.MONDAY)  // Same as existing schedule
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(16, 0))
                .build();

        mockMvc.perform(post("/doctors/{doctorId}/schedules", testDoctor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchedule_NotFound() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}/schedules/{scheduleId}", testDoctor.getId(), 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSchedule_NotFound() throws Exception {
        mockMvc.perform(delete("/api/doctors/{doctorId}/schedules/{scheduleId}", testDoctor.getId(), 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSchedule_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(post("/api/doctors/{doctorId}/schedules", testDoctor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSchedule_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(put("/api/doctors/{doctorId}/schedules/{scheduleId}", testDoctor.getId(), testSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSchedule_ForbiddenNoAdminRole() throws Exception {
        mockMvc.perform(delete("/api/doctors/{doctorId}/schedules/{scheduleId}", testDoctor.getId(), testSchedule.getId()))
                .andExpect(status().isForbidden());
    }
}
