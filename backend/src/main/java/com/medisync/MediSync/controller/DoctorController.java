package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.AppointmentDto;
import com.medisync.MediSync.dto.DoctorDto;
import com.medisync.MediSync.dto.DoctorRegistrationDto;
import com.medisync.MediSync.dto.DoctorUpdateDto;
import com.medisync.MediSync.service.AppointmentService;
import com.medisync.MediSync.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Management of doctor profiles and schedule availability.")
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "List all doctors", description = "Retrieves a list of doctors with optional filtering.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<DoctorDto>> getDoctors(@RequestParam(required = false) Long departmentId,
                                                      @RequestParam(defaultValue = "false") boolean deactivated) {
        return ResponseEntity.ok(doctorService.getDoctors(departmentId, deactivated));
    }

    @GetMapping("/{doctorId}")
    @Operation(summary = "Get doctor details", description = "Retrieves public profile information for a specific doctor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved doctor"),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    })
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable Long doctorId) {
        DoctorDto doctorDto = doctorService.getDoctorById(doctorId);
        return ResponseEntity.ok(doctorDto);
    }

    @GetMapping("/{doctorId}/appointments")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(
            summary = "Get doctor's appointments",
            description = "Retrieves all appointments for a specific doctor. Restricted to the Doctor themselves or Admins."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved appointments"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires DOCTOR or ADMIN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    })
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId));
    }

    @GetMapping("/{doctorId}/appointments/slots")
    @Operation(
            summary = "Get available time slots",
            description = "Returns a list of available appointment times for a doctor on a specific date."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved slots"),
            @ApiResponse(responseCode = "400", description = "Invalid date format", content = @Content),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    })
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new doctor", description = "Creates a new doctor profile and user account. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., email already exists)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    public ResponseEntity<DoctorDto> registerDoctor(@Valid @RequestBody DoctorRegistrationDto doctorRegistrationDto) {
        return new ResponseEntity<>(doctorService.registerDoctor(doctorRegistrationDto), HttpStatus.CREATED);
    }

    @PutMapping("/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update doctor details", description = "Updates profile information (specialization, etc.). Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor updated successfully"),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    public ResponseEntity<DoctorDto> updateDoctor(@PathVariable Long doctorId, @Valid @RequestBody DoctorUpdateDto doctorUpdateDto) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctorId, doctorUpdateDto));
    }

    @PutMapping("/{doctorId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate doctor", description = "Soft-deletes a doctor, preventing new bookings. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Doctor successfully deactivated (No Content)"),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    public ResponseEntity<DoctorDto> deactivateDoctor(@PathVariable Long doctorId) {
        doctorService.deactivateDoctor(doctorId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{doctorId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate doctor", description = "Restores a deactivated doctor account. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Doctor successfully activated (No Content)"),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    public ResponseEntity<DoctorDto> activate(@PathVariable Long doctorId) {
        doctorService.activateDoctor(doctorId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
