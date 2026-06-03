package com.medisync.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getDoctorWorkloadReport() {
        String sql = """
            SELECT 
                d.id AS doctor_id, 
                d.first_name, 
                d.last_name, 
                dep.name AS department_name, 
                COUNT(a.id) AS total_appointments 
            FROM doctors d 
            JOIN departments dep ON d.department_id = dep.id 
            LEFT JOIN appointments a ON a.doctor_id = d.id 
            GROUP BY d.id, d.first_name, d.last_name, dep.name 
            ORDER BY total_appointments DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getMonthlyAppointmentTrends() {
        String sql = """
            SELECT 
                TO_CHAR(appointment_time, 'YYYY-MM') AS month,
                COUNT(id) AS total_appointments
            FROM appointments
            GROUP BY TO_CHAR(appointment_time, 'YYYY-MM')
            ORDER BY month ASC
            LIMIT 12
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getDepartmentDistribution() {
        String sql = """
            SELECT 
                dep.name AS department_name, 
                COUNT(a.id) AS appointment_count
            FROM departments dep
            LEFT JOIN doctors d ON d.department_id = dep.id
            LEFT JOIN appointments a ON a.doctor_id = d.id
            GROUP BY dep.name
            ORDER BY appointment_count DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }
}
