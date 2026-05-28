package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AdminRegistrationDto;
import com.medisync.MediSync.dto.AdminUpdateDto;
import com.medisync.MediSync.dto.UserDto;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserDto registerAdmin(AdminRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalStateException("There is already an account associated with this email: " + registrationDto.getEmail());
        }

        User user = User.builder()
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .isActive(true)
                .role(Role.ADMIN)
                .build();

        return UserDto.mapToDto(userRepository.save(user));
    }

    public List<UserDto> getAllAdmins() {
        return userRepository.findAllByRole(Role.ADMIN).stream()
                .map(UserDto::mapToDto)
                .collect(Collectors.toList());
    }

    public UserDto updateAdmin(Long adminId, AdminUpdateDto updateDto) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalStateException("User is not an admin");
        }

        if (updateDto.getEmail() != null && !updateDto.getEmail().isBlank()) {
            if (!admin.getEmail().equals(updateDto.getEmail()) && userRepository.existsByEmail(updateDto.getEmail())) {
                throw new IllegalStateException("There is already an account associated with this email: " + updateDto.getEmail());
            }
            admin.setEmail(updateDto.getEmail());
        }

        if (updateDto.getPassword() != null && !updateDto.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        return UserDto.mapToDto(userRepository.save(admin));
    }

    public void deleteAdmin(Long adminId, Long currentUserId) throws BadRequestException {
        if (adminId.equals(currentUserId)) {
            throw new IllegalStateException("You cannot delete your own account.");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new BadRequestException("User is not an admin");
        }

        userRepository.delete(admin);
    }

}