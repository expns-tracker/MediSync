package com.medisync.MediSync.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@SuperBuilder
public class Department extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_doctor_id", referencedColumnName = "id", nullable = true)
    private Doctor departmentHead;
}
