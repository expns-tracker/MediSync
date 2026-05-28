package com.medisync.reporting;

import com.medisync.reporting.client.CoreServiceClient;
import com.medisync.reporting.dto.StatisticsDto;
import com.medisync.reporting.service.AnalyticsService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final CoreServiceClient coreServiceClient;
    private final AnalyticsService analyticsService;

    @GetMapping(value = {"/summary", "/summary/"})
    @CircuitBreaker(name = "coreService", fallbackMethod = "getStatisticsFallback")
    public StatisticsDto getSummary() {
        return coreServiceClient.getStatistics();
    }

    @GetMapping("/workload")
    public List<Map<String, Object>> getDoctorWorkload() {
        return analyticsService.getDoctorWorkloadReport();
    }

    @GetMapping("/trends")
    public List<Map<String, Object>> getMonthlyTrends() {
        return analyticsService.getMonthlyAppointmentTrends();
    }

    @GetMapping("/distribution")
    public List<Map<String, Object>> getDepartmentDistribution() {
        return analyticsService.getDepartmentDistribution();
    }

    public StatisticsDto getStatisticsFallback(Exception e) {
        // Return a default/empty DTO if core service is down
        return StatisticsDto.builder()
                .totalPatients(0)
                .totalDoctors(0)
                .totalAppointments(0)
                .totalDepartments(0)
                .build();
    }
}
