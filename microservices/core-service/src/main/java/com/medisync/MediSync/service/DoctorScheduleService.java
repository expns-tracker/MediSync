package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.DoctorScheduleCreateDto;
import com.medisync.MediSync.dto.DoctorScheduleDto;
import com.medisync.MediSync.entity.Doctor;
import com.medisync.MediSync.entity.DoctorSchedule;
import com.medisync.MediSync.exception.ResourceNotFoundException;
import com.medisync.MediSync.repository.DoctorRepository;
import com.medisync.MediSync.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;

    private final DoctorRepository doctorRepository;

    public List<DoctorScheduleDto> getSchedules(Long doctorId) {
        return doctorScheduleRepository.findByDoctorId(doctorId).stream()
                .map(DoctorScheduleDto::mapToDto)
                .toList();
    }

    public DoctorScheduleDto getSchedule(Long scheduleId) {
        return DoctorScheduleDto.mapToDto(doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new ResourceNotFoundException("Schedule with id " + scheduleId + " not found")));
    }

    public DoctorScheduleDto createSchedule(Long doctorId, DoctorScheduleCreateDto doctorScheduleCreateDto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (doctorScheduleRepository.existsByDoctorIdAndDayOfWeek(doctorId, doctorScheduleCreateDto.getDayOfWeek())) {
            throw new IllegalStateException("Doctor already has a schedule for " + doctorScheduleCreateDto.getDayOfWeek());
        }

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(doctorScheduleCreateDto.getDayOfWeek())
                .startTime(doctorScheduleCreateDto.getStartTime())
                .endTime(doctorScheduleCreateDto.getEndTime())
                .build();

        return DoctorScheduleDto.mapToDto(doctorScheduleRepository.save(schedule));
    }


    public DoctorScheduleDto updateSchedule(Long scheduleId, DoctorScheduleCreateDto doctorScheduleCreateDto) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        schedule.setStartTime(doctorScheduleCreateDto.getStartTime());
        schedule.setEndTime(doctorScheduleCreateDto.getEndTime());

        return DoctorScheduleDto.mapToDto(doctorScheduleRepository.save(schedule));
    }


    public void deleteSchedule(Long scheduleId, Long doctorId) {
        DoctorSchedule doctorSchedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule with id " + scheduleId + " not found"));

        if (!doctorSchedule.getDoctor().getId().equals(doctorId)) {
            throw new IllegalStateException("Schedule does  not belong to doctor");
        }

        doctorScheduleRepository.deleteById(scheduleId);
    }
}
