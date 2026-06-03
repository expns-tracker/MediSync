package com.medisync.MediSync.entity;

import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "appointments")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @OneToOne(mappedBy = "appointment")
    private MedicalRecord medicalRecord;

}
