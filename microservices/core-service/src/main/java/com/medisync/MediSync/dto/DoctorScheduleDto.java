package com.medisync.MediSync.dto;

import com.medisync.MediSync.entity.DoctorSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleDto {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long doctorId;
    private String doctorName;

    public static DoctorScheduleDto mapToDto(DoctorSchedule doctorSchedule) {
        return DoctorScheduleDto.builder()
                .id(doctorSchedule.getId())
                .dayOfWeek(doctorSchedule.getDayOfWeek())
                .startTime(doctorSchedule.getStartTime())
                .endTime(doctorSchedule.getEndTime())
                .doctorId(doctorSchedule.getDoctor().getId())
                .doctorName(doctorSchedule.getDoctor().getFirstName() + " " + doctorSchedule.getDoctor().getLastName())
                .build();
    }
}
