package com.medisync.MediSync.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentUpdateDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Long departmentHeadId;

}
