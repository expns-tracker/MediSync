package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.AuthTokenDto;
import com.medisync.MediSync.dto.CredentialsDto;
import com.medisync.MediSync.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Public endpoints for user authentication.")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements()
    @Operation(
            summary = "User Login",
            description = "Authenticates a user (Patient, Doctor, or Admin) using email and password. Returns a JWT token if successful."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful. JWT token returned."
            )
    })
    public ResponseEntity<AuthTokenDto> login(@Valid @RequestBody CredentialsDto credentials) {
        return ResponseEntity.ok(authService.login(credentials));
    }
}
