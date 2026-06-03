package com.medisync.MediSync.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "doctor_schedules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"doctor_id", "dayOfWeek"})
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;
}
