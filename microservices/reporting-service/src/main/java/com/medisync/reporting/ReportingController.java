package com.medisync.reporting;

import com.medisync.reporting.client.CoreServiceClient;
import com.medisync.reporting.dto.StatisticsDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping(value = {"/summary", "/summary/"})
    @CircuitBreaker(name = "coreService", fallbackMethod = "getStatisticsFallback")
    public StatisticsDto getSummary() {
        return coreServiceClient.getStatistics();
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
