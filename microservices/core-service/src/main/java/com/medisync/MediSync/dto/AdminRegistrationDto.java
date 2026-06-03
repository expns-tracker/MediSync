package com.medisync.MediSync.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminRegistrationDto {

    @Email(message = "Invalid email format")
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&()+=_-])(?=\\S+$).{8,}$",
            message = "Password must be strong (min 8 chars, 1 digit, 1 uppercase letter, 1 special char)"
    )
    private String password;
}
