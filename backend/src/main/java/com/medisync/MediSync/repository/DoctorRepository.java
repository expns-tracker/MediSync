package com.medisync.MediSync.repository;

import com.medisync.MediSync.entity.Doctor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByDepartmentId(Long departmentId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Optional<Doctor> findByIdWithLock(Long id);
    Collection<Doctor> findAllByUserIsActive(boolean isActive);
    Collection<Doctor> findByDepartmentIdAndUserIsActive(Long departmentId, boolean isActive);

    boolean existsByIdAndUserIsActive(Long doctorId,  boolean isActive);

    boolean existsByDepartmentId(Long id);
}
