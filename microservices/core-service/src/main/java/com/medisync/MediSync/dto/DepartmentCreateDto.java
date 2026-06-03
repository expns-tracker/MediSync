package com.medisync.MediSync.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentCreateDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

}
