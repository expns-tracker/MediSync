package com.medisync.MediSync.dto;

import com.medisync.MediSync.entity.Department;
import com.medisync.MediSync.entity.Doctor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {

    private Long id;

    private String name;

    private String description;

    private Long departmentHeadId;

    private String departmentHeadName;

    public static DepartmentDto mapToDto(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .departmentHeadId(department.getDepartmentHead() != null ?
                        department.getDepartmentHead().getId() :
                        null)
                .departmentHeadName(department.getDepartmentHead() != null ?
                        department.getDepartmentHead().getFirstName() + " " + department.getDepartmentHead().getLastName() :
                        null)
                .build();
    }
}
