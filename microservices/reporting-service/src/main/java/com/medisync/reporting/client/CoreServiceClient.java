package com.medisync.reporting.client;

import com.medisync.reporting.config.FeignConfig;
import com.medisync.reporting.dto.StatisticsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "core-service", url = "${core-service.url:http://localhost:8080}", configuration = FeignConfig.class)
public interface CoreServiceClient {

    @GetMapping("/api/statistics")
    StatisticsDto getStatistics();
}
