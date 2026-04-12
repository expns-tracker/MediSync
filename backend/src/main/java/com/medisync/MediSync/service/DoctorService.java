package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.DoctorDto;
import com.medisync.MediSync.dto.DoctorRegistrationDto;
import com.medisync.MediSync.dto.DoctorUpdateDto;
import com.medisync.MediSync.entity.Appointment;
import com.medisync.MediSync.entity.Department;
import com.medisync.MediSync.entity.Doctor;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.entity.enums.Specialization;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.AppointmentRepository;
import com.medisync.MediSync.repository.DepartmentRepository;
import com.medisync.MediSync.repository.DoctorRepository;
import com.medisync.MediSync.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;

    public DoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id " + id + " not found"));

        return DoctorDto.mapToDto(doctor);
    }

    public List<DoctorDto> getDoctors(Long departmentId, boolean deactivated) {
        if (departmentId == null) {
            return doctorRepository.findAllByUserIsActive(!deactivated).stream()
                    .map(DoctorDto::mapToDto).toList();
        }
        if(!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department with id " + departmentId + " not found");
        }
        return doctorRepository.findByDepartmentIdAndUserIsActive(departmentId, deactivated).stream()
                .map(DoctorDto::mapToDto).toList();
    }


    @Transactional
    public  DoctorDto registerDoctor(DoctorRegistrationDto doctorRegistrationDto) {

        if (userRepository.existsByEmail(doctorRegistrationDto.getEmail())){
            throw new IllegalStateException("There is already an account associated with this email: " + doctorRegistrationDto.getEmail());
        }

        User user = User.builder()
                .email(doctorRegistrationDto.getEmail())
                .password(passwordEncoder.encode(doctorRegistrationDto.getPassword()))
                .role(Role.DOCTOR)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        Department department = departmentRepository.findById(doctorRegistrationDto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Department with id=" + doctorRegistrationDto.getDepartmentId() + " not found"
                ));

        Doctor doctor = Doctor.builder()
                .firstName(doctorRegistrationDto.getFirstName())
                .lastName(doctorRegistrationDto.getLastName())
                .specialization(Specialization.valueOf(doctorRegistrationDto.getSpecialization().toUpperCase()))
                .appointmentDuration(AppointmentDuration.valueOf(doctorRegistrationDto.getAppointmentDuration().toUpperCase()))
                .user(user)
                .department(department)
                .build();

        return DoctorDto.mapToDto(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorDto updateDoctor(Long doctorId, DoctorUpdateDto doctorUpdateDto){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id " + doctorId + " not found"));

        Department department = departmentRepository.findById(doctorUpdateDto.getDepartmentId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Department with id=" + doctorUpdateDto.getDepartmentId() + " not found"
                        ));

        doctor.setFirstName(doctorUpdateDto.getFirstName());
        doctor.setLastName(doctorUpdateDto.getLastName());
        doctor.setSpecialization(Specialization.valueOf(doctorUpdateDto.getSpecialization().toUpperCase()));
        doctor.setAppointmentDuration(AppointmentDuration.valueOf(doctorUpdateDto.getAppointmentDuration().toUpperCase()));
        doctor.setDepartment(department);

        return DoctorDto.mapToDto(doctorRepository.save(doctor));
    }

    @Transactional
    public void deactivateDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id " + doctorId + " not found"));

        User user = doctor.getUser();
        if(!user.getIsActive()){
            throw new IllegalStateException("User is already not active for doctor with id " + doctorId);
        }

        List<Appointment> scheduledAppointments = appointmentRepository.findAllByDoctorIdAndStatus(
                doctorId,
                AppointmentStatus.SCHEDULED
        );

        for (Appointment appointment : scheduledAppointments) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
        }

        appointmentRepository.saveAll(scheduledAppointments);

        user.setIsActive(false);
        userRepository.save(user);
    }

    public void activateDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id " + doctorId + " not found"));

        User user = doctor.getUser();
        if(user.getIsActive()){
            throw new IllegalStateException("User is already active for doctor with id " + doctorId);
        }
        user.setIsActive(true);
        userRepository.save(user);
    }

}
