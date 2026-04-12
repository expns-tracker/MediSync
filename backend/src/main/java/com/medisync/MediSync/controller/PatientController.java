package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.AppointmentDto;
import com.medisync.MediSync.dto.PatientDto;
import com.medisync.MediSync.dto.PatientRegistrationDto;
import com.medisync.MediSync.dto.PatientUpdateDto;
import com.medisync.MediSync.service.AppointmentService;
import com.medisync.MediSync.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient self-registration, profile management, and medical history access.")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    @PostMapping
    @SecurityRequirements()
    @Operation(
            summary = "Register new patient",
            description = "Public endpoint allows new users to create a patient account. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., Email already in use, weak password)", content = @Content)
    })
    public ResponseEntity<String> registerPatient(@Valid @RequestBody PatientRegistrationDto patientRegistrationDto) {
        patientService.registerPatient(patientRegistrationDto);

        return new ResponseEntity<>("Patient Registration Successful", HttpStatus.CREATED);
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Get patient profile", description = "Retrieves public profile information of a patient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved profile"),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
    })
    public ResponseEntity<PatientDto> getPatientById(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getById(patientId));
    }

    @PutMapping("/{patientId}/")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(
            summary = "Update profile",
            description = "Allows a patient to update their personal details (address, phone, etc.). Restricted to the account owner."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You can only update your own profile", content = @Content),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
    })
    public ResponseEntity<PatientDto> updatePatient(
            @PathVariable Long patientId,
            @Valid @RequestBody PatientUpdateDto patientUpdateDto,
            Principal principal) {
        return ResponseEntity.ok(patientService.updatePatient(patientId, patientUpdateDto, principal.getName()));
    }

    @GetMapping("/{patientId}/appointments")
    @Operation(
            summary = "Get patient's appointments",
            description = "Retrieves the appointment history for a specific patient. Access is restricted to the Patient (owner) or an Admin."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved appointment history"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to view this patient's history", content = @Content),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
    })
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByPatientId(@PathVariable Long patientId,
                                                                           Principal principal) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(patientId, principal.getName()));
    }

    @PutMapping("/{patientId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate patient", description = "Soft-deletes a patient account. Restricted to Admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient successfully deactivated (No Content)"),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    public ResponseEntity<Void> deactivatePatient(@PathVariable Long patientId) {
        patientService.deactivatePatient(patientId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{patientId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate patient", description = "Restores a deactivated patient account. Restricted to Admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient successfully activated (No Content)"),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    public ResponseEntity<Void> activatePatient(@PathVariable Long patientId) {
        patientService.activatePatient(patientId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
