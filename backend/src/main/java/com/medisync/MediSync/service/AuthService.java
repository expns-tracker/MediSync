package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AuthTokenDto;
import com.medisync.MediSync.dto.CredentialsDto;
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

    public AuthTokenDto login(CredentialsDto credentials) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credentials.getEmail(),
                        credentials.getPassword()
                )
        );

        String email = authentication.getName();

        return new AuthTokenDto(jwtService.generateToken(email));
    }
}
