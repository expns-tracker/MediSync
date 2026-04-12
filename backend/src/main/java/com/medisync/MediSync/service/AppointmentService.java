package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AppointmentBookDto;
import com.medisync.MediSync.dto.AppointmentDto;
import com.medisync.MediSync.dto.MedicalRecordCreateDto;
import com.medisync.MediSync.dto.MedicalRecordDto;
import com.medisync.MediSync.entity.*;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;

    public AppointmentDto findById(Long id, String currentUserEmail) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment with id=" + id + " not found."));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwnerPatient = appointment.getPatient().getUser().getEmail().equals(currentUserEmail);
        boolean isAssignedDoctor = appointment.getDoctor().getUser().getEmail().equals(currentUserEmail);

        if (!isAdmin && !isOwnerPatient && !isAssignedDoctor) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to access this appointment."
            );
        }

        return AppointmentDto.mapToDto(appointment);
    }

    public List<AppointmentDto> getPatientAppointments(Long patientId, String currentUserEmail) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with id=" + patientId + " not found."));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isPatient = currentUser.getRole() == Role.PATIENT;
        boolean isOwnerPatient = patient.getUser().getEmail().equals(currentUserEmail);

        if (isPatient && !isOwnerPatient) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to access this resource."
            );
        }

        List<Appointment> appointments = appointmentRepository.findByPatient(patient);
        return appointments.stream().map(AppointmentDto::mapToDto).toList();
    }

    public List<AppointmentDto> getDoctorAppointments(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id=" + doctorId + " not found."));

        List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);
        return appointments.stream().map(AppointmentDto::mapToDto).toList();
    }

    public List<LocalTime> getAvailableSlots(Long doctorId, LocalDate date) {

        if (date.isBefore(LocalDate.now())) {
            return Collections.emptyList();
        }

        if (!doctorRepository.existsByIdAndUserIsActive(doctorId, true)) {
            throw new ResourceNotFoundException("No active doctor with id=" + doctorId + " found.");
        }

        DoctorSchedule doctorSchedule = doctorScheduleRepository
                .findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor is not working on " + date.getDayOfWeek()));

        Doctor doctor = doctorSchedule.getDoctor();
        int duration = doctor.getAppointmentDuration().getMinutes();

        LocalDateTime startTime = doctorSchedule.getStartTime().atDate(date);
        LocalDateTime endTime  = doctorSchedule.getEndTime().atDate(date);

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, startTime, endTime
        );

        Set<LocalTime> bookedSlots = appointments.stream()
                .map(appointment -> appointment.getAppointmentTime().toLocalTime())
                .collect(Collectors.toSet());

        List<LocalTime> availableSlots = new ArrayList<>();

        for (
                LocalTime slot = doctorSchedule.getStartTime();
                slot.plusMinutes(duration).isBefore(doctorSchedule.getEndTime())||
                slot.plusMinutes(duration).equals(doctorSchedule.getEndTime());
                slot = slot.plusMinutes(duration)
        ) {
            if (date.equals(LocalDate.now()) && slot.isBefore(LocalTime.now())) {
                continue;
            }

            if (!bookedSlots.contains(slot)) {
                availableSlots.add(slot);
            }
        }

        return availableSlots;
    }

    @Transactional
    public AppointmentDto bookAppointment(AppointmentBookDto appointmentBookDto){

        Doctor doctor = doctorRepository.findByIdWithLock(appointmentBookDto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id=" + appointmentBookDto.getDoctorId() + " not found."));

        if (!doctor.getUser().getIsActive()) {
            throw new IllegalStateException("Doctor account is not active.");
        }

        AppointmentDuration appointmentDuration = doctor.getAppointmentDuration();

        LocalDateTime startTime = appointmentBookDto.getAppointmentTime();
        LocalDateTime endTime = appointmentBookDto.getAppointmentTime().plusMinutes(appointmentDuration.getMinutes());

        DayOfWeek dayOfWeek = appointmentBookDto.getAppointmentTime().getDayOfWeek();

        DoctorSchedule doctorSchedule = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek)
                .orElseThrow(() -> new IllegalArgumentException("Doctor is not working on " + dayOfWeek));

        if(startTime.toLocalTime().isBefore(doctorSchedule.getStartTime())
                || endTime.toLocalTime().isAfter(doctorSchedule.getEndTime())){
            throw new IllegalArgumentException("Requested time is outside of doctor's working hours (" +
                    doctorSchedule.getStartTime() + " - " + doctorSchedule.getEndTime() + ")");
        }

        long minutesFromStart = java.time.Duration.between(
                doctorSchedule.getStartTime(),
                startTime.toLocalTime()
        ).toMinutes();

        if (minutesFromStart % appointmentDuration.getMinutes() != 0) {
            throw new IllegalArgumentException(
                    "Appointment time is not aligned with doctor's schedule. The schedule starts at " +
                            doctorSchedule.getStartTime() + " with slots of " +
                    appointmentDuration.getMinutes() + " mins). Valid slots are like " +
                    doctorSchedule.getStartTime().plusMinutes(appointmentDuration.getMinutes()));
        }

        boolean notAvailable = appointmentRepository.existsByDoctorAppointmentTime(
                doctor.getId(),
                appointmentBookDto.getAppointmentTime()
        );

        if (notAvailable){
            throw new IllegalStateException("Doctor is already booked for this time slot.");
        }

        Patient patient = patientRepository.findById(appointmentBookDto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient with id=" + appointmentBookDto.getPatientId() + " not found."));

        Appointment appointment = Appointment.builder()
                .appointmentTime(appointmentBookDto.getAppointmentTime())
                .reason(appointmentBookDto.getReason())
                .status(AppointmentStatus.SCHEDULED)
                .doctor(doctor)
                .patient(patient)
                .build();

        return AppointmentDto.mapToDto(appointmentRepository.save(appointment));
    }

    @Transactional
    public MedicalRecordDto completeAppointment(Long appointmentId, MedicalRecordCreateDto medicalRecordCreateDto, String currentUserEmail){
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment with id=" + appointmentId + " not found")
                );

        userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAssignedDoctor = appointment.getDoctor().getUser().getEmail().equals(currentUserEmail);

        if (!isAssignedDoctor) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to complete this appointment."
            );
        }

        if (!appointment.getStatus().equals(AppointmentStatus.SCHEDULED)){
            throw new IllegalStateException(
                    "Cannot complete appointment with"
                            + AppointmentStatus.COMPLETED
                            + ", " + AppointmentStatus.CANCELLED
                            + " or " + AppointmentStatus.NO_SHOW + " status."
            );
        }

        if (appointment.getMedicalRecord() != null){
            throw new IllegalStateException("A medical record already exists for this appointment");
        }

        if (appointment.getAppointmentTime().isAfter(LocalDateTime.now())){
            throw new IllegalStateException("Cannot complete an appointment that hasn't started yet");
        }

        MedicalRecord medicalRecord = MedicalRecord.builder()
                .appointment(appointment)
                .diagnosis(medicalRecordCreateDto.getDiagnosis())
                .treatmentPlan(medicalRecordCreateDto.getTreatmentPlan())
                .prescription(medicalRecordCreateDto.getPrescription())
                .build();

        medicalRecord = medicalRecordRepository.save(medicalRecord);

        appointment.setStatus(AppointmentStatus.COMPLETED);

        appointmentRepository.save(appointment);

        return MedicalRecordDto.mapToDto(medicalRecord);

    }

    public AppointmentDto cancelAppointment(Long appointmentId, String currentUserEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment with id=" + appointmentId + " not found"));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        boolean isOwnerPatient = appointment.getPatient().getUser().getEmail().equals(currentUserEmail);

        boolean isAssignedDoctor = appointment.getDoctor().getUser().getEmail().equals(currentUserEmail);

        if (!isAdmin && !isOwnerPatient && !isAssignedDoctor) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to cancel this appointment."
            );
        }

        if(!appointment.getStatus().equals(AppointmentStatus.SCHEDULED)){
            throw new IllegalStateException(
                    "Cannot complete appointment with"
                    + AppointmentStatus.COMPLETED
                    + ", " + AppointmentStatus.CANCELLED
                    + " or " + AppointmentStatus.NO_SHOW + " status."
            );
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentDto.mapToDto(appointmentRepository.save(appointment));
    }

    public AppointmentDto markNoShow(Long appointmentId, String currentUserEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment with id=" + appointmentId + " not found"));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        boolean isAssignedDoctor = appointment.getDoctor().getUser().getEmail().equals(currentUserEmail);

        if (!isAdmin && !isAssignedDoctor) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to update this appointment."
            );
        }

        if(!appointment.getStatus().equals(AppointmentStatus.SCHEDULED)){
            throw new IllegalStateException(
                    "Cannot mark as NO_SHOW appointment with"
                            + AppointmentStatus.COMPLETED
                            + ", " + AppointmentStatus.CANCELLED
                            + " or " + AppointmentStatus.NO_SHOW + " status."
            );
        }

        if (appointment.getAppointmentTime().plusMinutes(
                appointment.getDoctor().getAppointmentDuration().getMinutes()
        ).isAfter(LocalDateTime.now())){
            throw new IllegalStateException("Cannot mark appointment with "
                    + AppointmentStatus.NO_SHOW
                    + " since it's not finished yet");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        return AppointmentDto.mapToDto(appointmentRepository.save(appointment));
    }
}
