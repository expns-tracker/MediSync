package com.medisync.MediSync.repository;

import com.medisync.MediSync.entity.Appointment;
import com.medisync.MediSync.entity.Doctor;
import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByDoctor(Doctor doctor);

    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.status != 'CANCELLED'
        AND a.appointmentTime = :appointmentTime
    """)
    boolean existsByDoctorAppointmentTime(Long doctorId, LocalDateTime appointmentTime);

    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            Long doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay
    );

    List<Appointment> findAllByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    List<Appointment> findAllByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
}
