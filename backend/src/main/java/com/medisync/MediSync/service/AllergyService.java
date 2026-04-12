package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AllergyCreateDto;
import com.medisync.MediSync.dto.AllergyDto;
import com.medisync.MediSync.entity.Allergy;
import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.enums.AllergyCategory;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.AllergyRepository;
import com.medisync.MediSync.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AllergyService {

    private final AllergyRepository allergyRepository;
    private final PatientRepository patientRepository;

    public List<AllergyDto> getAllAllergies() {
        return allergyRepository.findAll().stream()
                .map(AllergyDto::mapToDto)
                .toList();
    }

    public AllergyDto getAllergy(Long allergyId) {
        return AllergyDto.mapToDto(allergyRepository.findById(allergyId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergy with id=" + allergyId + " not found")));
    }

    public AllergyDto createAllergy(AllergyCreateDto allergyCreateDto) {
        Allergy allergy = Allergy.builder()
                .name(allergyCreateDto.getName())
                .code(allergyCreateDto.getCode())
                .category(AllergyCategory.valueOf(allergyCreateDto.getCategory().toUpperCase()))
                .build();

        return AllergyDto.mapToDto(allergyRepository.save(allergy));
    }

    public AllergyDto updateAllergy(Long allergyId, AllergyCreateDto allergyCreateDto) {
        Allergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergy with id=" + allergyId + " not found"));

        allergy.setName(allergyCreateDto.getName());
        allergy.setCode(allergyCreateDto.getCode());
        allergy.setCategory(AllergyCategory.valueOf(allergyCreateDto.getCategory().toUpperCase()));

        return AllergyDto.mapToDto(allergyRepository.save(allergy));
    }

    @Transactional
    public void deleteAllergy(Long allergyId) {
        Allergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergy with id=" + allergyId + " not found"));

        List<Patient> patientsWithAllergy = patientRepository.findAllByAllergiesContaining(allergy);

        for (Patient patient : patientsWithAllergy) {
            patient.getAllergies().remove(allergy);
        }

        patientRepository.saveAll(patientsWithAllergy);

        allergyRepository.deleteById(allergyId);
    }
}
