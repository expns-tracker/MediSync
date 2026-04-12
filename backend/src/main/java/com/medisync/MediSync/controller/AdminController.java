package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.AdminRegistrationDto;
import com.medisync.MediSync.dto.UserDto;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Operations for system administrators to manage other admins and view system stats.")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "List all admins", description = "Retrieves a list of all users with ADMIN role.")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @Operation(summary = "Register new admin", description = "Creates a new system administrator account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserDto> registerAdmin(@Valid @RequestBody AdminRegistrationDto registrationDto) {
        return new ResponseEntity<>(adminService.registerAdmin(registrationDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Delete admin", description = "Removes an administrator account. NOTE: You cannot delete yourself.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Admin deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request (e.g., trying to delete yourself)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Admin not found", content = @Content)
    })
    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> deleteAdmin(
            @PathVariable Long adminId,
            @AuthenticationPrincipal User currentUser
    ) throws BadRequestException {
        adminService.deleteAdmin(adminId, currentUser.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}