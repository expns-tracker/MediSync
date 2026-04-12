package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.DepartmentCreateDto;
import com.medisync.MediSync.dto.DepartmentDto;
import com.medisync.MediSync.dto.DepartmentUpdateDto;
import com.medisync.MediSync.entity.Department;
import com.medisync.MediSync.entity.Doctor;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.DepartmentRepository;
import com.medisync.MediSync.repository.DoctorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;


    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentDto::mapToDto)
                .toList();
    }


    public DepartmentDto getDepartmentById(Long id) {
        return DepartmentDto.mapToDto(departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department with id=" + id + "not found.")));
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department with id=" + id + " not found.");
        }

        if (doctorRepository.existsByDepartmentId(id)) {
            throw new IllegalStateException("Can't delete the department since it has doctors associated with it");
        };

        departmentRepository.deleteById(id);
    }

    public DepartmentDto createDepartment(DepartmentCreateDto departmentCreateDto) {
        if (departmentRepository.existsByName(departmentCreateDto.getName())) {
            throw new IllegalStateException("Department with name " + departmentCreateDto.getName() + " already exists.");
        }

        return DepartmentDto.mapToDto(departmentRepository.save(Department.builder()
                .name(departmentCreateDto.getName())
                .description(departmentCreateDto.getDescription())
                .build()));
    }

    public DepartmentDto updateDepartment(Long departmentId, DepartmentUpdateDto departmentUpdateDto) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department with id=" + departmentId + " not found."));

        if (!department.getName().equals(departmentUpdateDto.getName()) && departmentRepository.existsByName(department.getName())) {
            throw new IllegalStateException("Department with name " + department.getName() + " already exists.");
        }

        Doctor departmentHead = null;
        if (departmentUpdateDto.getDepartmentHeadId() != null) {
            departmentHead = doctorRepository.findById(departmentUpdateDto.getDepartmentHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor with id=" + departmentUpdateDto.getDepartmentHeadId() + " not found."));

            if (!departmentHead.getDepartment().getId().equals(departmentId)) {
                throw new IllegalStateException("Doctor with id=" + departmentHead.getId() + " does not belong to this department.");
            }
        }

        department.setName(departmentUpdateDto.getName());
        department.setDescription(departmentUpdateDto.getDescription());
        department.setDepartmentHead(departmentHead);

        return DepartmentDto.mapToDto(departmentRepository.save(department));
    }
}
