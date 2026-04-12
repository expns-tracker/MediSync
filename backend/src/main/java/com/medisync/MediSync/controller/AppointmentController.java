package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.AppointmentBookDto;
import com.medisync.MediSync.dto.AppointmentDto;
import com.medisync.MediSync.dto.MedicalRecordCreateDto;
import com.medisync.MediSync.dto.MedicalRecordDto;
import com.medisync.MediSync.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Endpoints for booking, viewing, and managing medical appointments.")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/{appointmentId}")
    @Operation(
            summary = "Get appointment details",
            description = "Retrieves details of a specific appointment. Access is restricted to the owning Patient, the assigned Doctor, or an Admin."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved appointment details"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not associated with this appointment", content = @Content),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content)
    })
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long appointmentId, Principal principal) {
        return ResponseEntity.ok(appointmentService.findById(appointmentId, principal.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(
            summary = "Book a new appointment",
            description = "Allows a Patient to schedule an appointment with a doctor. Requires PATIENT role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment successfully booked"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., Doctor not found, invalid time slot)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only patients can book appointments", content = @Content)
    })
    public ResponseEntity<AppointmentDto> bookAppointment(@Valid @RequestBody AppointmentBookDto appointmentDto) {
        return new ResponseEntity<>(appointmentService.bookAppointment(appointmentDto), HttpStatus.CREATED);
    }

    @PostMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(
            summary = "Complete an appointment",
            description = "Marks an appointment as COMPLETED and generates the associated medical record. Requires DOCTOR role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment completed and record created"),
            @ApiResponse(responseCode = "409", description = "Conflict - Appointment is already cancelled or completed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only the assigned doctor can complete this appointment", content = @Content),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content)
    })
    public ResponseEntity<MedicalRecordDto> completeAppointment(
            @Valid @RequestBody MedicalRecordCreateDto medicalRecordCreateDto,
            @PathVariable Long appointmentId,
            Principal principal
    ) {
        return ResponseEntity.ok(appointmentService.completeAppointment(appointmentId,medicalRecordCreateDto, principal.getName()));
    }

    @PutMapping("/{appointmentId}/cancel")
    @Operation(
            summary = "Cancel an appointment",
            description = "Cancels a scheduled appointment. Access is restricted to the owning Patient, the assigned Doctor, or an Admin."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment successfully cancelled"),
            @ApiResponse(responseCode = "409", description = "Conflict - Appointment is already completed or cancelled", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to cancel this appointment", content = @Content),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content)
    })
    public ResponseEntity<AppointmentDto> cancelAppointment(@PathVariable Long appointmentId, Principal principal) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId, principal.getName()));
    }

    @PutMapping("/{appointmentId}/no-show")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(
            summary = "Mark appointment as No-Show",
            description = "Marks an appointment as NO_SHOW if the patient did not attend. Requires DOCTOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment marked as no-show"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires DOCTOR or ADMIN role", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict - Appointment status cannot be changed (e.g., already completed)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content)
    })
    public ResponseEntity<AppointmentDto> markNoShow(@PathVariable Long appointmentId, Principal principal) {
        return ResponseEntity.ok(appointmentService.markNoShow(appointmentId, principal.getName()));
    }

}
