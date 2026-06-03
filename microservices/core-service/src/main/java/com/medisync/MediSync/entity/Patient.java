package com.medisync.MediSync.entity;

import com.medisync.MediSync.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "patients")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Patient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String phoneNumber;

    private String address;
    private String city;
    private String county;
    private String country;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToMany
    @JoinTable(name = "patients_allergies")
    private List<Allergy> allergies;
}
