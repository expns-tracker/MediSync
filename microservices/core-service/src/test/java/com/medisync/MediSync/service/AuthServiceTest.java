package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AuthTokenDto;
import com.medisync.MediSync.dto.CredentialsDto;
import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.entity.enums.Role;
import com.medisync.MediSync.repository.DoctorRepository;
import com.medisync.MediSync.repository.PatientRepository;
import com.medisync.MediSync.repository.UserRepository;
import com.medisync.MediSync.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks
    private AuthService authService;

    private CredentialsDto credentials;
    private User patientUser;
    private User doctorUser;
    private Patient patient;
    private com.medisync.MediSync.entity.Doctor doctor;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        credentials = CredentialsDto.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        patientUser = User.builder()
                .id(1L)
                .email("patient@example.com")
                .role(Role.PATIENT)
                .isActive(true)
                .build();

        doctorUser = User.builder()
                .id(2L)
                .email("doctor@example.com")
                .role(Role.DOCTOR)
                .isActive(true)
                .build();

        patient = Patient.builder()
                .id(1L)
                .user(patientUser)
                .build();

        doctor = com.medisync.MediSync.entity.Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .build();

        authentication = mock(Authentication.class);
    }

    @Test
    void login_Patient_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("patient@example.com");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(jwtService.generateToken(eq("patient@example.com"), eq("PATIENT"), eq(1L), eq(1L), eq(null)))
                .thenReturn("jwt-token");

        // When
        AuthTokenDto result = authService.login(credentials);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("patient@example.com");
        verify(patientRepository).findByUserId(1L);
        verify(doctorRepository, never()).findByUserId(any());
        verify(jwtService).generateToken("patient@example.com", "PATIENT", 1L, 1L, null);
    }

    @Test
    void login_Doctor_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("doctor@example.com");
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(jwtService.generateToken(eq("doctor@example.com"), eq("DOCTOR"), eq(2L), eq(null), eq(1L)))
                .thenReturn("jwt-token");

        // When
        AuthTokenDto result = authService.login(credentials);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("doctor@example.com");
        verify(doctorRepository).findByUserId(2L);
        verify(patientRepository, never()).findByUserId(any());
        verify(jwtService).generateToken("doctor@example.com", "DOCTOR", 2L, null, 1L);
    }

    @Test
    void login_Admin_Success() {
        // Given
        User adminUser = User.builder()
                .id(3L)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(jwtService.generateToken(eq("admin@example.com"), eq("ADMIN"), eq(3L), eq(null), eq(null)))
                .thenReturn("jwt-token");

        // When
        AuthTokenDto result = authService.login(credentials);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("admin@example.com");
        verify(patientRepository, never()).findByUserId(any());
        verify(doctorRepository, never()).findByUserId(any());
        verify(jwtService).generateToken("admin@example.com", "ADMIN", 3L, null, null);
    }

    @Test
    void login_UserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(credentials))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Authenticated user not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("unknown@example.com");
        verify(jwtService, never()).generateToken(any(), any(), any(), any(), any());
    }

    @Test
    void login_PatientRecordNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("patient@example.com");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(credentials))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Patient record for authenticated user not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("patient@example.com");
        verify(patientRepository).findByUserId(1L);
        verify(jwtService, never()).generateToken(any(), any(), any(), any(), any());
    }

    @Test
    void login_DoctorRecordNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("doctor@example.com");
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(credentials))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Doctor record for authenticated user not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("doctor@example.com");
        verify(doctorRepository).findByUserId(2L);
        verify(jwtService, never()).generateToken(any(), any(), any(), any(), any());
    }
}
