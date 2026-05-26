package com.medisync.MediSync.repository;

import com.medisync.MediSync.entity.Allergy;
import com.medisync.MediSync.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findAllByAllergiesContaining(Allergy allergy);
    Optional<Patient> findByUserId(Long userId);

    @Query("""
        SELECT p FROM Patient p 
        WHERE (:search IS NULL 
             OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) 
             OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) 
             OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
    """)
    Page<Patient> findAllPatientsWithSearch(@Param("search") String search, Pageable pageable);
}
