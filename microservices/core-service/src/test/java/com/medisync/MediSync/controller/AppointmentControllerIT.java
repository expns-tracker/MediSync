package com.medisync.MediSync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medisync.MediSync.dto.AppointmentBookDto;
import com.medisync.MediSync.dto.MedicalRecordCreateDto;
import com.medisync.MediSync.entity.*;
import com.medisync.MediSync.entity.enums.*;
import com.medisync.MediSync.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppointmentControllerIT {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private DoctorScheduleRepository scheduleRepository;

    private Long doctorId;
    private Long patientId;
    private Long scheduledAppointmentId;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.save(
                Department.builder()
                        .name("Cardiology")
                        .description("Heart")
                        .build()
        );

        User docUser = userRepository.save(User.builder()
                .email("doc@test.com")
                .password("pass")
                .role(Role.DOCTOR)
                .isActive(true)
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .user(docUser)
                .department(dept)
                .firstName("John").lastName("Doe")
                .specialization(Specialization.CARDIOLOGY)
                .appointmentDuration(AppointmentDuration.MINUTES_30)
                .build());
        doctorId = doctor.getId();

        User patUser = userRepository.save(User.builder()
                .email("pat@test.com")
                .password("pass")
                .role(Role.PATIENT)
                .isActive(true)
                .build());

        Patient patient = patientRepository.save(Patient.builder()
                .user(patUser)
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.now().minusYears(25))
                .gender(Gender.FEMALE)
                .phoneNumber("0000000000")
                .build());

        patientId = patient.getId();

        scheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build());

        Appointment appt = appointmentRepository.save(Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .status(AppointmentStatus.SCHEDULED)
                .appointmentTime(LocalDateTime.now().minusHours(1))
                .reason("Initial Checkup")
                .build());
        scheduledAppointmentId = appt.getId();
    }

    @Test
    @DisplayName("GET /api/appointments/{id} - Success")
    void getAppointmentById_Success() throws Exception {
        mockMvc.perform(get("/api/appointments/{id}", scheduledAppointmentId)
                        .with(user("doc@test.com").roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(scheduledAppointmentId))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} - Not Found")
    void getAppointmentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/appointments/{id}", 9999L)
                        .with(user("doc@test.com").roles("DOCTOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/appointments - Success")
    void bookAppointment_Success() throws Exception {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime time = LocalDateTime.of(nextMonday, LocalTime.of(10, 0));

        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(doctorId);
        dto.setPatientId(patientId);
        dto.setAppointmentTime(time);
        dto.setReason("Chest Pain");

        mockMvc.perform(post("/api/appointments")
                        .with(user("pat@test.com").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.reason").value("Chest Pain"));
    }

    @Test
    @DisplayName("POST /api/appointments - Fail Validation (Empty Body)")
    void bookAppointment_ValidationFail() throws Exception {
        AppointmentBookDto dto = new AppointmentBookDto();

        mockMvc.perform(post("/api/appointments")
                        .with(user("pat@test.com").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/appointments - Fail Business Logic (Slot not aligned)")
    void bookAppointment_LogicFail() throws Exception {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime time = LocalDateTime.of(nextMonday, LocalTime.of(9, 15));

        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(doctorId);
        dto.setPatientId(patientId);
        dto.setAppointmentTime(time);
        dto.setReason("Test");

        mockMvc.perform(post("/api/appointments")
                        .with(user("pat@test.com").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /complete - Success")
    void completeAppointment_Success() throws Exception {
        MedicalRecordCreateDto recordDto = new MedicalRecordCreateDto(
                "Flu", "Rest and Water", "Paracetamol"
        );

        mockMvc.perform(post("/api/appointments/{id}/complete", scheduledAppointmentId)
                        .with(user("doc@test.com").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recordDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("Flu"));
    }

    @Test
    @DisplayName("PUT /cancel - Success")
    void cancelAppointment_Success() throws Exception {
        mockMvc.perform(put("/api/appointments/{id}/cancel", scheduledAppointmentId)
                        .with(user("pat@test.com").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PUT /no-show - Success")
    void markNoShow_Success() throws Exception {
        mockMvc.perform(put("/api/appointments/{id}/no-show", scheduledAppointmentId)
                        .with(user("doc@test.com").roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }
}