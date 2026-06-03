package com.medisync.MediSync.repository;

import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email);

    List<User> findAllByRole(Role role);
}
