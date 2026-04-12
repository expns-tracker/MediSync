package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.PatientDto;
import com.medisync.MediSync.dto.PatientRegistrationDto;
import com.medisync.MediSync.dto.PatientUpdateDto;
import com.medisync.MediSync.entity.Allergy;
import com.medisync.MediSync.entity.Appointment;
import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.entity.enums.Gender;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.AllergyRepository;
import com.medisync.MediSync.repository.AppointmentRepository;
import com.medisync.MediSync.repository.PatientRepository;
import com.medisync.MediSync.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AllergyRepository allergyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public void registerPatient(PatientRegistrationDto patientRegistrationDto) {

        if (userRepository.existsByEmail(patientRegistrationDto.getEmail())){
            throw new IllegalStateException("There is already an account associated with this email: " + patientRegistrationDto.getEmail());
        }

        User user = User.builder()
                .email(patientRegistrationDto.getEmail())
                .password(passwordEncoder.encode(patientRegistrationDto.getPassword()))
                .role(Role.PATIENT)
                .isActive(true)
                .build();
        userRepository.save(user);

        Patient patient = Patient.builder()
                .firstName(patientRegistrationDto.getFirstName())
                .lastName(patientRegistrationDto.getLastName())
                .gender(Gender.valueOf(patientRegistrationDto.getGender().toUpperCase()))
                .phoneNumber(patientRegistrationDto.getPhoneNumber())
                .dateOfBirth(patientRegistrationDto.getDateOfBirth())
                .address(patientRegistrationDto.getAddress())
                .city(patientRegistrationDto.getCity())
                .county(patientRegistrationDto.getCounty())
                .country(patientRegistrationDto.getCountry())
                .user(user)
                .build();

        if (
                patientRegistrationDto.getAllergyIds() != null && !patientRegistrationDto.getAllergyIds().isEmpty()
        ) {
            List<Allergy> allergies = allergyRepository.findAllById(patientRegistrationDto.getAllergyIds());
            patient.setAllergies(allergies);
        }

        patientRepository.save(patient);

    }

    public PatientDto getById(Long patientId) {
        return PatientDto.mapToDto(patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with id=" + patientId + " not found!")));
    }

    @Transactional
    public PatientDto updatePatient(Long patientId, PatientUpdateDto patientUpdateDto, String currentUserEmail) {
        Patient patient = patientRepository.findById(patientId).orElseThrow(
                () -> new ResourceNotFoundException("Patient with id=" + patientId + " not found!")
        );

        if (!userRepository.existsByEmail(currentUserEmail)){
            throw new IllegalStateException("There is no account associated with this email: " + currentUserEmail);
        }

        if (!patient.getUser().getEmail().equals(currentUserEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to access perform this action."
            );
        }

        patient.setFirstName(patientUpdateDto.getFirstName());
        patient.setLastName(patientUpdateDto.getLastName());
        patient.setGender(patientUpdateDto.getGender());
        patient.setPhoneNumber(patientUpdateDto.getPhoneNumber());
        patient.setDateOfBirth(patientUpdateDto.getDateOfBirth());
        patient.setAddress(patientUpdateDto.getAddress());
        patient.setCity(patientUpdateDto.getCity());
        patient.setCounty(patientUpdateDto.getCounty());
        patient.setCountry(patientUpdateDto.getCountry());

        if (
                patientUpdateDto.getAllergyIds() != null
        ) {
            List<Allergy> allergies = allergyRepository.findAllById(patientUpdateDto.getAllergyIds());
            patient.getAllergies().clear();
            patient.getAllergies().addAll(allergies);
        }

        patientRepository.save(patient);
        return PatientDto.mapToDto(patient);
    }

    @Transactional
    public void deactivatePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with id=" + patientId + " not found!"));

        User user = patient.getUser();

        if (!user.getIsActive()){
            throw new IllegalStateException("User is already not active for patient with id=" + patientId);
        }

        List<Appointment> scheduledAppointments = appointmentRepository.
                findAllByPatientIdAndStatus(patientId, AppointmentStatus.SCHEDULED);

        for (Appointment appointment : scheduledAppointments) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
        }

        appointmentRepository.saveAll(scheduledAppointments);

        user.setIsActive(false);
        userRepository.save(user);
    }

    public void activatePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with id=" + patientId + " not found!"));

        User user = patient.getUser();

        if (user.getIsActive()){
            throw new IllegalStateException("User is already active for patient with id=" + patientId);
        }

        user.setIsActive(true);
        userRepository.save(user);
    }
}
