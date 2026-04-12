package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AppointmentBookDto;
import com.medisync.MediSync.dto.AppointmentDto;
import com.medisync.MediSync.dto.MedicalRecordCreateDto;
import com.medisync.MediSync.dto.MedicalRecordDto;
import com.medisync.MediSync.entity.*;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private DoctorScheduleRepository doctorScheduleRepository;
    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Doctor doctor;
    private Patient patient;
    private DoctorSchedule schedule;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        User doctorUser = User.builder().id(1L).email("doctor@test.com").isActive(true).build();
        User patientUser = User.builder().id(2L).email("patient@test.com").isActive(true).build();

        Department department = Department.builder()
                .id(1L)
                .name("Department")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .appointmentDuration(AppointmentDuration.MINUTES_30)
                .specialization(Specialization.CARDIOLOGY)
                .department(department)
                .build();

        patient = Patient.builder().id(2L).user(patientUser).build();

        schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        appointment = Appointment.builder()
                .id(100L)
                .doctor(doctor)
                .patient(patient)
                .status(AppointmentStatus.SCHEDULED)
                .appointmentTime(LocalDateTime.of(2025, 1, 1, 9, 0)) // Past date for completion tests
                .build();


    }

    @Test
    void findById_Success() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getDoctor().getUser()));

        AppointmentDto result = appointmentService.findById(100L, appointment.getDoctor().getUser().getEmail());
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void findById_NotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.findById(999L, appointment.getDoctor().getUser().getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPatientAppointments_Success() {
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByPatient(patient)).thenReturn(List.of(appointment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getPatient().getUser()));

        List<AppointmentDto> results = appointmentService.getPatientAppointments(2L,
                appointment.getPatient().getUser().getEmail());
        assertThat(results).hasSize(1);
    }

    @Test
    void getPatientAppointments_PatientNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.getPatientAppointments(99L,
                appointment.getPatient().getUser().getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDoctorAppointments_Success() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctor(doctor)).thenReturn(List.of(appointment));

        List<AppointmentDto> results = appointmentService.getDoctorAppointments(1L);
        assertThat(results).hasSize(1);
    }

    @Test
    void getDoctorAppointments_DoctorNotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.getDoctorAppointments(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // Tests for getAvailableSlots

    @Test
    void getAvailableSlots_DateInPast_ReturnsEmpty() {
        List<LocalTime> slots = appointmentService.getAvailableSlots(1L, LocalDate.now().minusDays(1));
        assertThat(slots).isEmpty();
    }

    @Test
    void getAvailableSlots_DoctorNotActive_ThrowsException() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        when(doctorRepository.existsByIdAndUserIsActive(1L, true)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.getAvailableSlots(1L, futureDate))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No active doctor");
    }

    @Test
    void getAvailableSlots_NoScheduleForDay_ThrowsException() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        DayOfWeek day = futureDate.getDayOfWeek();

        when(doctorRepository.existsByIdAndUserIsActive(1L, true)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, day)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAvailableSlots(1L, futureDate))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor is not working on");
    }

    @Test
    void getAvailableSlots_Success() {

        LocalDate date = LocalDate.now().plusDays(1);

        when(doctorRepository.existsByIdAndUserIsActive(1L, true)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, date.getDayOfWeek()))
                .thenReturn(Optional.of(schedule));

        List<LocalTime> slots = appointmentService.getAvailableSlots(1L, date);

        assertThat(slots).isNotEmpty();
    }

    @Test
    void getAvailableSlots_AllSlotsBooked_ReturnsEmpty() {


        LocalDate date = LocalDate.now().plusDays(1);
        List<Appointment> fullDayAppointments = new ArrayList<>();

        LocalTime currentTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(10, 0);

        while (currentTime.isBefore(endTime)) {
            fullDayAppointments.add(Appointment.builder()
                    .id((long) fullDayAppointments.size() + 1)
                    .doctor(doctor)
                    .patient(patient)
                    .status(AppointmentStatus.SCHEDULED)
                    .appointmentTime(LocalDateTime.of(date, currentTime))
                    .build());

            currentTime = currentTime.plusMinutes(30);
        }

        when(doctorRepository.existsByIdAndUserIsActive(1L, true)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, date.getDayOfWeek()))
                .thenReturn(Optional.of(schedule));
        when(appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(any(), any(), any()))
                .thenReturn(fullDayAppointments);

        List<LocalTime> slots = appointmentService.getAvailableSlots(1L, date);

        assertThat(slots).isEmpty();


    }

    // Tests for bookAppointment

    @Test
    void bookAppointment_DoctorNotFound() {
        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(99L);
        when(doctorRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void bookAppointment_DoctorInactive() {
        doctor.getUser().setIsActive(false);
        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(1L);

        when(doctorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void bookAppointment_NoScheduleForDay() {
        LocalDate nextTues = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.TUESDAY));
        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(1L);
        dto.setAppointmentTime(LocalDateTime.of(nextTues, LocalTime.of(9, 0)));

        when(doctorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.TUESDAY))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not working on");
    }

    @Test
    void bookAppointment_TimeOutsideWorkingHours() {
        LocalDate nextMon = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime tryBookDate = LocalDateTime.of(nextMon, LocalTime.of(8, 0));

        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(1L);
        dto.setAppointmentTime(tryBookDate);

        when(doctorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside of doctor's working hours");
    }

    @Test
    void bookAppointment_TimeMisaligned() {
        LocalDate nextMon = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime misaligned = LocalDateTime.of(nextMon, LocalTime.of(9, 15));

        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(1L);
        dto.setAppointmentTime(misaligned);

        when(doctorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not aligned");
    }

    @Test
    void bookAppointment_SlotAlreadyBooked() {
        LocalDate nextMon = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime validTime = LocalDateTime.of(nextMon, LocalTime.of(9, 0));

        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(1L);
        dto.setAppointmentTime(validTime);

        when(doctorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(schedule));

        when(appointmentRepository.existsByDoctorAppointmentTime(1L, validTime)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void bookAppointment_PatientNotFound() {
        LocalDate nextMon = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime validTime = LocalDateTime.of(nextMon, LocalTime.of(9, 0));

        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(1L);
        dto.setPatientId(99L);
        dto.setAppointmentTime(validTime);

        when(doctorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(schedule));
        when(appointmentRepository.existsByDoctorAppointmentTime(1L, validTime)).thenReturn(false);
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient with id=99 not found");
    }

    @Test
    void bookAppointment_Success() {
        LocalDate nextMon = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime validTime = LocalDateTime.of(nextMon, LocalTime.of(9, 0));

        AppointmentBookDto dto = new AppointmentBookDto();
        dto.setDoctorId(1L);
        dto.setPatientId(2L);
        dto.setAppointmentTime(validTime);

        when(doctorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(schedule));
        when(appointmentRepository.existsByDoctorAppointmentTime(1L, validTime)).thenReturn(false);
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        AppointmentDto res = appointmentService.bookAppointment(dto);
        assertThat(res.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    // Tests for completeAppointment

    @Test
    void completeAppointment_NotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.completeAppointment(99L, new MedicalRecordCreateDto(), appointment.getDoctor().getUser().getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void completeAppointment_WrongStatus() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getDoctor().getUser()));

        assertThatThrownBy(() -> appointmentService.completeAppointment(100L, new MedicalRecordCreateDto(), appointment.getDoctor().getUser().getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete appointment with" +
                        AppointmentStatus.COMPLETED + ", " +
                        AppointmentStatus.CANCELLED + " or " +
                        AppointmentStatus.NO_SHOW + " status.");
    }

    @Test
    void completeAppointment_AlreadyHasMedicalRecord() {
        appointment.setMedicalRecord(new MedicalRecord());
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getDoctor().getUser()));

        assertThatThrownBy(() -> appointmentService.completeAppointment(100L, new MedicalRecordCreateDto(), appointment.getDoctor().getUser().getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("A medical record already exists for this appointment");
    }

    @Test
    void completeAppointment_FutureDate_CannotComplete() {
        appointment.setAppointmentTime(LocalDateTime.now().plusDays(1));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getDoctor().getUser()));

        assertThatThrownBy(() -> appointmentService.completeAppointment(100L, new MedicalRecordCreateDto(), appointment.getDoctor().getUser().getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete an appointment that hasn't started yet");
    }

    @Test
    void completeAppointment_Success() {
        appointment.setAppointmentTime(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(medicalRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getDoctor().getUser()));

        MedicalRecordCreateDto dto = new MedicalRecordCreateDto("Flu", "Rest", "Meds");
        MedicalRecordDto result = appointmentService.completeAppointment(100L, dto, appointment.getDoctor().getUser().getEmail());

        assertThat(result.getDiagnosis()).isEqualTo("Flu");
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    // Tests for cancelAppointment

    @Test
    void cancelAppointment_NotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.cancelAppointment(99L, appointment.getPatient().getUser().getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelAppointment_WrongStatus() {
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getPatient().getUser()));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(100L, appointment.getPatient().getUser().getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete appointment with" +
                        AppointmentStatus.COMPLETED + ", " +
                        AppointmentStatus.CANCELLED + " or " +
                        AppointmentStatus.NO_SHOW + " status.");
    }

    @Test
    void cancelAppointment_Success() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getPatient().getUser()));

        AppointmentDto result = appointmentService.cancelAppointment(100L, appointment.getPatient().getUser().getEmail());
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    // Tests for markNoShow

    @Test
    void markNoShow_NotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.markNoShow(99L, appointment.getDoctor().getUser().getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markNoShow_WrongStatus() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getDoctor().getUser()));

        assertThatThrownBy(() -> appointmentService.markNoShow(100L, appointment.getDoctor().getUser().getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot mark as NO_SHOW appointment with" +
                        AppointmentStatus.COMPLETED + ", " +
                        AppointmentStatus.CANCELLED + " or " +
                        AppointmentStatus.NO_SHOW + " status.");
    }

    @Test
    void markNoShow_NotFinishedYet() {
        appointment.setAppointmentTime(LocalDateTime.now().minusMinutes(10));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getDoctor().getUser()));

        assertThatThrownBy(() -> appointmentService.markNoShow(100L, appointment.getDoctor().getUser().getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot mark appointment with " +
                        AppointmentStatus.NO_SHOW +
                        " since it's not finished yet");
    }

    @Test
    void markNoShow_Success() {
        appointment.setAppointmentTime(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(appointment.getDoctor().getUser()));

        AppointmentDto result = appointmentService.markNoShow(100L, appointment.getDoctor().getUser().getEmail());
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
    }
}