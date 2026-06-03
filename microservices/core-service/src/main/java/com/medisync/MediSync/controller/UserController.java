package com.medisync.MediSync.controller;

import com.medisync.MediSync.dto.ChangePasswordDto;
import com.medisync.MediSync.dto.UserDto;
import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "General user account operations, including password management.")
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user details", description = "Retrieves basic user information (Role, Email, Status).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PatchMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Updates the password for the currently authenticated user. Requires providing the old password for verification."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., Old password incorrect", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not authenticated", content = @Content)
    })
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.changePassword(request, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
