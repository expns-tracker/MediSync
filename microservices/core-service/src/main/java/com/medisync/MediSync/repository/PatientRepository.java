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

    @Query(value = """
        SELECT p.*
        FROM patients p
        JOIN users u ON u.id = p.user_id
        WHERE u.is_active = true
          AND (:search IS NULL
               OR lower(p.first_name::text) LIKE lower('%' || CAST(:search AS text) || '%')
               OR lower(p.last_name::text) LIKE lower('%' || CAST(:search AS text) || '%')
               OR lower(u.email::text) LIKE lower('%' || CAST(:search AS text) || '%'))
        """, nativeQuery = true)
    Page<Patient> findActivePatientsWithSearch(@Param("search") String search, Pageable pageable);
}
