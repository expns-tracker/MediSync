package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AuthTokenDto;
import com.medisync.MediSync.dto.CredentialsDto;
import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.repository.DoctorRepository;
import com.medisync.MediSync.repository.PatientRepository;
import com.medisync.MediSync.repository.UserRepository;
import com.medisync.MediSync.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AuthTokenDto login(CredentialsDto credentials) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credentials.getEmail(),
                        credentials.getPassword()
                )
        );

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Long patientId = null;
        Long doctorId = null;

        if (user.getRole().name().equals("PATIENT")) {
            Patient patient = patientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalStateException("Patient record for authenticated user not found"));
            patientId = patient.getId();
        }

        if (user.getRole().name().equals("DOCTOR")) {
            doctorId = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalStateException("Doctor record for authenticated user not found"))
                    .getId();
        }

        return new AuthTokenDto(jwtService.generateToken(email, user.getRole().name(), user.getId(), patientId, doctorId));
    }
}
