package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.ChangePasswordDto;
import com.medisync.MediSync.dto.UserDto;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private ChangePasswordDto changePasswordDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.PATIENT)
                .isActive(true)
                .build();

        changePasswordDto = ChangePasswordDto.builder()
                .currentPassword("currentPass123")
                .newPassword("newPass456")
                .build();
    }

    @Test
    void getUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserDto result = userService.getUser(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo(Role.PATIENT.name());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUser_NotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUser(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id=1 not found");

        verify(userRepository).findById(1L);
    }

    @Test
    void changePassword_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPass123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass456")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.changePassword(changePasswordDto, 1L);

        // Then
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("currentPass123", "encodedPassword");
        verify(passwordEncoder).encode("newPass456");
        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    void changePassword_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(changePasswordDto, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User id=1 not found");

        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_IncorrectCurrentPassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPass123", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(changePasswordDto, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Incorrect current password");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("currentPass123", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
