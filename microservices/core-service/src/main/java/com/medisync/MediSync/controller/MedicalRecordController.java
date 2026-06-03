package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.MedicalRecordCreateDto;
import com.medisync.MediSync.dto.MedicalRecordDto;
import com.medisync.MediSync.repository.MedicalRecordRepository;
import com.medisync.MediSync.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Tag(name = "Medical Records", description = "Management of diagnosis, prescriptions, and treatment plans. Restricted to Doctors.")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    @PutMapping("/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(
            summary = "Update medical record",
            description = "Modifies an existing medical record (e.g., updating diagnosis or prescription). Restricted to the assigned Doctor."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medical record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., missing diagnosis)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires DOCTOR role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Medical record not found", content = @Content)
    })
    public ResponseEntity<MedicalRecordDto> updateMedicalRecord(
            @PathVariable Long medicalRecordId,
            @Valid @RequestBody MedicalRecordCreateDto medicalRecordCreateDto
    ) {
        return ResponseEntity.ok(medicalRecordService.updateMedicalRecord(medicalRecordId, medicalRecordCreateDto));
    }

    @DeleteMapping("/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(
            summary = "Delete medical record",
            description = "Removes a medical record entry. Caution: This action is permanent. Restricted to the assigned Doctor."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Medical record deleted successfully (No Content)"),
            @ApiResponse(responseCode = "404", description = "Medical record not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires DOCTOR role", content = @Content)
    })
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable Long medicalRecordId) {
        medicalRecordService.deleteMedicalRecord(medicalRecordId);
        return ResponseEntity.noContent().build();
    }

}
