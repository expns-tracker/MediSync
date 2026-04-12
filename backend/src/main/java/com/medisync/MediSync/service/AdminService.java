package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AdminRegistrationDto;
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