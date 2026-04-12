package com.medisync.MediSync.repository;

import com.medisync.MediSync.entity.Allergy;
import com.medisync.MediSync.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findAllByAllergiesContaining(Allergy allergy);
}
