package com.medisync.MediSync.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentUpdateDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Long departmentHeadId;

}
