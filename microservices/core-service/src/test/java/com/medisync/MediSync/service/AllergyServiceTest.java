package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.AllergyCreateDto;
import com.medisync.MediSync.dto.AllergyDto;
import com.medisync.MediSync.entity.Allergy;
import com.medisync.MediSync.entity.Patient;
import com.medisync.MediSync.entity.enums.AllergyCategory;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.AllergyRepository;
import com.medisync.MediSync.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllergyServiceTest {

    @Mock private AllergyRepository allergyRepository;
    @Mock private PatientRepository patientRepository;

    @InjectMocks
    private AllergyService allergyService;

    private Allergy allergy;
    private AllergyCreateDto allergyCreateDto;

    @BeforeEach
    void setUp() {
        allergy = Allergy.builder()
                .id(1L)
                .name("Penicillin")
                .code("PEN")
                .category(AllergyCategory.MEDICATION)
                .build();

        allergyCreateDto = new AllergyCreateDto();
        allergyCreateDto.setName("Penicillin");
        allergyCreateDto.setCode("PEN");
        allergyCreateDto.setCategory("MEDICATION");
    }

    // Tests for getAllAllergies

    @Test
    void getAllAllergies_Success() {
        Allergy allergy2 = Allergy.builder()
                .id(2L)
                .name("Peanuts")
                .code("PNT")
                .category(AllergyCategory.FOOD)
                .build();

        when(allergyRepository.findAll()).thenReturn(List.of(allergy, allergy2));

        List<AllergyDto> results = allergyService.getAllAllergies();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Penicillin");
        assertThat(results.get(1).getName()).isEqualTo("Peanuts");
        verify(allergyRepository, times(1)).findAll();
    }

    @Test
    void getAllAllergies_Empty() {
        when(allergyRepository.findAll()).thenReturn(new ArrayList<>());

        List<AllergyDto> results = allergyService.getAllAllergies();

        assertThat(results).isEmpty();
        verify(allergyRepository, times(1)).findAll();
    }

    // Tests for getAllergy

    @Test
    void getAllergy_Success() {
        when(allergyRepository.findById(1L)).thenReturn(Optional.of(allergy));

        AllergyDto result = allergyService.getAllergy(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Penicillin");
        assertThat(result.getCode()).isEqualTo("PEN");
        assertThat(result.getCategory()).isEqualTo("MEDICATION");
        verify(allergyRepository, times(1)).findById(1L);
    }

    @Test
    void getAllergy_NotFound() {
        when(allergyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allergyService.getAllergy(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allergy with id=99 not found");
        verify(allergyRepository, times(1)).findById(99L);
    }

    // Tests for createAllergy

    @Test
    void createAllergy_Success() {
        when(allergyRepository.save(any(Allergy.class))).thenReturn(allergy);

        AllergyDto result = allergyService.createAllergy(allergyCreateDto);

        assertThat(result.getName()).isEqualTo("Penicillin");
        assertThat(result.getCode()).isEqualTo("PEN");
        assertThat(result.getCategory()).isEqualTo("MEDICATION");
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void createAllergy_WithLowerCaseCategory() {
        allergyCreateDto.setCategory("food");

        Allergy foodAllergy = Allergy.builder()
                .id(2L)
                .name("Peanuts")
                .code("PNT")
                .category(AllergyCategory.FOOD)
                .build();

        when(allergyRepository.save(any(Allergy.class))).thenReturn(foodAllergy);

        AllergyDto result = allergyService.createAllergy(allergyCreateDto);

        assertThat(result.getCategory()).isEqualTo("FOOD");
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void createAllergy_AllCategories() {
        String[] categories = {"MEDICATION", "FOOD", "ENVIRONMENTAL", "ANIMAL", "INSECT", "OTHER"};

        for (int i = 0; i < categories.length; i++) {
            allergyCreateDto.setCategory(categories[i]);
            allergyCreateDto.setCode("CODE_" + i);

            Allergy testAllergy = Allergy.builder()
                    .id((long) i)
                    .name("Test" + i)
                    .code("CODE_" + i)
                    .category(AllergyCategory.valueOf(categories[i]))
                    .build();

            when(allergyRepository.save(any(Allergy.class))).thenReturn(testAllergy);

            AllergyDto result = allergyService.createAllergy(allergyCreateDto);

            assertThat(result.getCategory()).isEqualTo(categories[i]);
            verify(allergyRepository, times(i + 1)).save(any(Allergy.class));
        }
    }

    // Tests for updateAllergy

    @Test
    void updateAllergy_Success() {
        AllergyCreateDto updateDto = new AllergyCreateDto();
        updateDto.setName("Amoxicillin");
        updateDto.setCode("AMX");
        updateDto.setCategory("MEDICATION");

        when(allergyRepository.findById(1L)).thenReturn(Optional.of(allergy));
        when(allergyRepository.save(any(Allergy.class))).thenAnswer(i -> i.getArguments()[0]);

        AllergyDto result = allergyService.updateAllergy(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Amoxicillin");
        assertThat(result.getCode()).isEqualTo("AMX");
        verify(allergyRepository, times(1)).findById(1L);
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_NotFound() {
        when(allergyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allergyService.updateAllergy(99L, allergyCreateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allergy with id=99 not found");
        verify(allergyRepository, times(1)).findById(99L);
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_UpdatesCategory() {
        AllergyCreateDto updateDto = new AllergyCreateDto();
        updateDto.setName("Penicillin");
        updateDto.setCode("PEN");
        updateDto.setCategory("FOOD");

        Allergy allergyWithNewCategory = Allergy.builder()
                .id(1L)
                .name("Penicillin")
                .code("PEN")
                .category(AllergyCategory.FOOD)
                .build();

        when(allergyRepository.findById(1L)).thenReturn(Optional.of(allergy));
        when(allergyRepository.save(any(Allergy.class))).thenReturn(allergyWithNewCategory);

        AllergyDto result = allergyService.updateAllergy(1L, updateDto);

        assertThat(result.getCategory()).isEqualTo("FOOD");
        verify(allergyRepository, times(1)).findById(1L);
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    // Tests for deleteAllergy

    @Test
    void deleteAllergy_Success_NoAssociatedPatients() {
        when(allergyRepository.findById(1L)).thenReturn(Optional.of(allergy));
        when(patientRepository.findAllByAllergiesContaining(allergy)).thenReturn(new ArrayList<>());

        allergyService.deleteAllergy(1L);

        verify(allergyRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findAllByAllergiesContaining(allergy);
        verify(patientRepository, times(1)).saveAll(new ArrayList<>());
        verify(allergyRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteAllergy_NotFound() {
        when(allergyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allergyService.deleteAllergy(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allergy with id=99 not found");
        verify(allergyRepository, times(1)).findById(99L);
        verify(allergyRepository, never()).deleteById(any());
    }

    @Test
    void deleteAllergy_RemovesAllergyFromPatients() {
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setAllergies(new ArrayList<>(List.of(allergy)));

        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setAllergies(new ArrayList<>(List.of(allergy)));

        List<Patient> patientsWithAllergy = List.of(patient1, patient2);

        when(allergyRepository.findById(1L)).thenReturn(Optional.of(allergy));
        when(patientRepository.findAllByAllergiesContaining(allergy)).thenReturn(patientsWithAllergy);

        allergyService.deleteAllergy(1L);

        verify(allergyRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findAllByAllergiesContaining(allergy);
        verify(patientRepository, times(1)).saveAll(patientsWithAllergy);
        verify(allergyRepository, times(1)).deleteById(1L);

        assertThat(patient1.getAllergies()).isEmpty();
        assertThat(patient2.getAllergies()).isEmpty();
    }

    @Test
    void deleteAllergy_MultiplePatientsMixedAllergies() {
        Allergy allergy2 = Allergy.builder()
                .id(2L)
                .name("Peanuts")
                .code("PNT")
                .category(AllergyCategory.FOOD)
                .build();

        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setAllergies(new ArrayList<>(List.of(allergy, allergy2)));

        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setAllergies(new ArrayList<>(List.of(allergy)));

        List<Patient> patientsWithAllergy = List.of(patient1, patient2);

        when(allergyRepository.findById(1L)).thenReturn(Optional.of(allergy));
        when(patientRepository.findAllByAllergiesContaining(allergy)).thenReturn(patientsWithAllergy);

        allergyService.deleteAllergy(1L);

        verify(patientRepository, times(1)).saveAll(patientsWithAllergy);
        verify(allergyRepository, times(1)).deleteById(1L);

        assertThat(patient1.getAllergies()).containsExactly(allergy2);
        assertThat(patient2.getAllergies()).isEmpty();
    }
}


