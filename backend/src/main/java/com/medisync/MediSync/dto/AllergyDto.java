package com.medisync.MediSync.dto;

import com.medisync.MediSync.entity.Allergy;
import com.medisync.MediSync.entity.enums.AllergyCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergyDto {

    private Long id;
    private String name;
    private String code;
    private String category;

    public static AllergyDto mapToDto(Allergy allergy) {
        return AllergyDto.builder()
                .id(allergy.getId())
                .name(allergy.getName())
                .code(allergy.getCode())
                .category(allergy.getCategory().name())
                .build();
    }
}
