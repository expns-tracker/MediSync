package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.AllergyCreateDto;
import com.medisync.MediSync.dto.AllergyDto;
import com.medisync.MediSync.service.AllergyService;
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
@RequestMapping("api/allergies")
@RequiredArgsConstructor
@Tag(name = "Allergies", description = "Allergy catalog management. Viewing is authenticated; modification requires ADMIN role.")
public class AllergyController {
    private final AllergyService allergyService;

    @GetMapping
    @Operation(summary = "List all allergies", description = "Retrieves the list of all defined allergies in the system.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AllergyDto>> getAllAllergies() {
        return ResponseEntity.ok(allergyService.getAllAllergies());
    }

    @GetMapping("/{allergyId}")
    @Operation(summary = "Get allergy by ID", description = "Retrieves details for a specific allergy.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved allergy"),
            @ApiResponse(responseCode = "404", description = "Allergy not found", content = @Content)
    })
    public ResponseEntity<AllergyDto> getAllergy(@PathVariable Long allergyId) {
        return ResponseEntity.ok(allergyService.getAllergy(allergyId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new allergy", description = "Adds a new allergy to the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Allergy created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g. missing name or duplicate code)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    public ResponseEntity<AllergyDto> createAllergy(@Valid @RequestBody AllergyCreateDto allergyCreateDto) {
        return new ResponseEntity<>(allergyService.createAllergy(allergyCreateDto), HttpStatus.CREATED);
    }

    @PutMapping("/{allergyId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update allergy", description = "Updates details of an existing allergy. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Allergy updated successfully"),
            @ApiResponse(responseCode = "404", description = "Allergy not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    public ResponseEntity<AllergyDto> updateAllergy(@PathVariable Long allergyId,
                                                    @Valid @RequestBody AllergyCreateDto allergyCreateDto) {
        return ResponseEntity.ok(allergyService.updateAllergy(allergyId, allergyCreateDto));
    }

    @Operation(summary = "Delete allergy", description = "Removes an allergy from the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Allergy deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Allergy not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    @DeleteMapping("/{allergyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllergy(@PathVariable Long allergyId) {
        allergyService.deleteAllergy(allergyId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
