package com.medisync.MediSync.repository;

import com.medisync.MediSync.entity.Appointment;
import com.medisync.MediSync.entity.Doctor;
import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("""
        SELECT a FROM Appointment a 
        JOIN FETCH a.doctor d
        JOIN FETCH d.user du
        JOIN FETCH d.department dept
        WHERE a.patient.id = :patientId 
        AND (:timeframe = 'all' 
             OR (:timeframe = 'past' AND a.appointmentTime < :now) 
             OR (:timeframe = 'upcoming' AND a.appointmentTime >= :now))
    """)
    Page<Appointment> findPatientAppointmentsFiltered(
            @Param("patientId") Long patientId,
            @Param("timeframe") String timeframe,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("""
        SELECT a FROM Appointment a 
        JOIN FETCH a.patient p
        JOIN FETCH p.user pu
        WHERE a.doctor.id = :doctorId 
        AND (:timeframe = 'all' 
             OR (:timeframe = 'past' AND a.appointmentTime < :now) 
             OR (:timeframe = 'upcoming' AND a.appointmentTime >= :now))
    """)
    Page<Appointment> findDoctorAppointmentsFiltered(
            @Param("doctorId") Long doctorId,
            @Param("timeframe") String timeframe,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

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

    long countByStatus(AppointmentStatus status);

    @Query("""
        SELECT a FROM Appointment a 
        JOIN FETCH a.patient p
        JOIN FETCH p.user pu
        JOIN FETCH a.doctor d
        JOIN FETCH d.user du
        JOIN FETCH d.department dept
        WHERE (:status IS NULL OR a.status = :status)
        AND (:search IS NULL 
             OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) 
             OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(d.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
    """)
    Page<Appointment> findAllWithFilters(
            @Param("status") AppointmentStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
