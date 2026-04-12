package com.medisync.MediSync.dto;

import com.medisync.MediSync.entity.enums.AllergyCategory;
import com.medisync.MediSync.entity.enums.AppointmentDuration;
import com.medisync.MediSync.validation.ValueOfEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AllergyCreateDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Category is required")
    @ValueOfEnum(enumClass = AllergyCategory.class, message = "Category must be one of the following: " +
            "MEDICATION, " +
            "FOOD, " +
            "ENVIRONMENTAL, " +
            "ANIMAL, " +
            "INSECT, " +
            "OTHER"
    )
    private String category;
}
