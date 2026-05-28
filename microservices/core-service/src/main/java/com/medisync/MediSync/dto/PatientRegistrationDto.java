package com.medisync.MediSync.dto;


import com.medisync.MediSync.entity.enums.Gender;
import com.medisync.MediSync.validation.ValueOfEnum;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class PatientRegistrationDto {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&()+=_-])(?=\\S+$).{8,}$",
            message = "Password must be strong (min 8 chars, 1 digit, 1 uppercase letter, 1 special char)"
    )
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    @ValueOfEnum(enumClass = Gender.class, message = "Gender must be one of the following: MALE, FEMALE or OTHER")
    private String gender;

    private List<Long> allergyIds;

    private String address;
    private String city;
    private String county;
    private String country;

}
