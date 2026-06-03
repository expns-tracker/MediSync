package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.MedicalRecordCreateDto;
import com.medisync.MediSync.dto.MedicalRecordDto;
import com.medisync.MediSync.entity.Appointment;
import com.medisync.MediSync.entity.MedicalRecord;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.AppointmentRepository;
import com.medisync.MediSync.repository.MedicalRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public void deleteMedicalRecord(Long medicalRecordId) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                        .orElseThrow(() -> new ResourceNotFoundException("Medical Record Not Found"));
        Appointment appointment = medicalRecord.getAppointment();

        appointment.setMedicalRecord(null);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        medicalRecordRepository.deleteById(medicalRecordId);
        appointmentRepository.save(appointment);
    }

    public MedicalRecordDto updateMedicalRecord(Long medicalRecordId, MedicalRecordCreateDto medicalRecordCreateDto) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical Record Not Found"));

        medicalRecord.setDiagnosis(medicalRecordCreateDto.getDiagnosis());
        medicalRecord.setTreatmentPlan(medicalRecordCreateDto.getTreatmentPlan());
        medicalRecord.setPrescription(medicalRecordCreateDto.getPrescription());

        return MedicalRecordDto.mapToDto(medicalRecordRepository.save(medicalRecord));
    }

}
