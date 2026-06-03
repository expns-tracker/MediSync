package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.DepartmentCreateDto;
import com.medisync.MediSync.dto.DepartmentDto;
import com.medisync.MediSync.dto.DepartmentUpdateDto;
import com.medisync.MediSync.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Management of hospital departments (e.g., Cardiology, Neurology).")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "List all departments", description = "Retrieves a list of all available medical departments.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @Operation(summary = "Get department details", description = "Retrieves information about a specific department.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved department"),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> getDepartmentById(
            @Parameter(description = "ID of the department")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @Operation(summary = "Create department", description = "Adds a new department to the hospital. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Department created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., missing name)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentCreateDto createDto) {
        return new ResponseEntity<>(departmentService.createDepartment(createDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update department", description = "Modifies an existing department's details. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDto> updateDepartment(
            @Parameter(description = "ID of the department to update")
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateDto departmentUpdateDto
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, departmentUpdateDto));
    }

    @Operation(
            summary = "Delete department",
            description = "Removes a department. Note: Cannot delete if doctors are currently assigned to it. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Department deleted successfully (No Content)"),
            @ApiResponse(responseCode = "409", description = "Conflict - Cannot delete department with assigned doctors", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "ID of the department to delete")
            @PathVariable Long id
    ) {
        departmentService.deleteDepartment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}