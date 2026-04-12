package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.DoctorScheduleCreateDto;
import com.medisync.MediSync.dto.DoctorScheduleDto;
import com.medisync.MediSync.service.DoctorScheduleService;
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

import java.util.List;

@RestController
@RequestMapping("/doctors/{doctorId}/schedules")
@RequiredArgsConstructor
@Tag(name = "Doctor Schedules", description = "Management of doctor working hours and recurring weekly shifts.")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @GetMapping
    @Operation(
            summary = "Get doctor's weekly schedule",
            description = "Retrieves all defined working shifts (e.g., Mon 09:00-17:00, Wed 10:00-14:00) for a specific doctor."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved schedule list"),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    })
    public ResponseEntity<List<DoctorScheduleDto>> getFullSchedule(@PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorScheduleService.getSchedules(doctorId));
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "Get specific shift details", description = "Retrieves details of a single schedule entry.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved schedule"),
            @ApiResponse(responseCode = "404", description = "Schedule or Doctor not found", content = @Content)
    })
    public ResponseEntity<DoctorScheduleDto> getSchedule(@PathVariable Long doctorId, @PathVariable Long scheduleId) {
        return ResponseEntity.ok(doctorScheduleService.getSchedule(scheduleId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Add working hours",
            description = "Creates a new recurring weekly shift for a doctor. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schedule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., Start time after End time)", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict - Overlaps with an existing shift", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    })
    public ResponseEntity<DoctorScheduleDto> createSchedule(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorScheduleCreateDto doctorScheduleCreateDto) {
        return new ResponseEntity<>(doctorScheduleService.createSchedule(doctorId, doctorScheduleCreateDto), HttpStatus.CREATED);
    }

    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update working hours",
            description = "Modifies an existing shift (e.g., changing times). Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedule updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict - Overlaps with an existing shift", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Schedule not found", content = @Content)
    })
    public ResponseEntity<DoctorScheduleDto> updateSchedule(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody DoctorScheduleCreateDto doctorScheduleCreateDto) {
        return ResponseEntity.ok(doctorScheduleService.updateSchedule(scheduleId, doctorScheduleCreateDto));
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove working hours", description = "Deletes a working shift from the doctor's schedule. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Schedule deleted successfully (No Content)"),
            @ApiResponse(responseCode = "404", description = "Schedule not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId) {
        doctorScheduleService.deleteSchedule(scheduleId, doctorId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}