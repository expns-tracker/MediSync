package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AdminRegistrationDto;
import com.medisync.MediSync.dto.UserDto;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;
    private AdminRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        registrationDto = AdminRegistrationDto.builder()
                .email("newadmin@example.com")
                .password("password123")
                .build();
    }

    @Test
    void registerAdmin_Success() {
        // Given
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        // When
        UserDto result = adminService.registerAdmin(registrationDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("admin@example.com");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN.name());
        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(passwordEncoder).encode(registrationDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerAdmin_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> adminService.registerAdmin(registrationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("There is already an account associated with this email: " + registrationDto.getEmail());

        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllAdmins_Success() {
        // Given
        List<User> admins = List.of(adminUser);
        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(admins);

        // When
        List<UserDto> result = adminService.getAllAdmins();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("admin@example.com");
        assertThat(result.get(0).getRole()).isEqualTo(Role.ADMIN.name());
        verify(userRepository).findAllByRole(Role.ADMIN);
    }

    @Test
    void getAllAdmins_Empty() {
        // Given
        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(List.of());

        // When
        List<UserDto> result = adminService.getAllAdmins();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(userRepository).findAllByRole(Role.ADMIN);
    }

    @Test
    void deleteAdmin_Success() throws BadRequestException {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        // When
        adminService.deleteAdmin(1L, 2L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(adminUser);
    }

    @Test
    void deleteAdmin_CannotDeleteSelf() {
        // When & Then
        assertThatThrownBy(() -> adminService.deleteAdmin(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You cannot delete your own account.");

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteAdmin_AdminNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.deleteAdmin(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Admin not found");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteAdmin_UserNotAdmin() {
        // Given
        User nonAdminUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .role(Role.PATIENT)
                .isActive(true)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(nonAdminUser));

        // When & Then
        assertThatThrownBy(() -> adminService.deleteAdmin(1L, 2L))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any());
    }
}
