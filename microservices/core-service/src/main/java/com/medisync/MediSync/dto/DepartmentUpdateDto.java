package com.medisync.MediSync.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class DepartmentUpdateDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Long departmentHeadId;

}

