package com.medisync.MediSync.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private Map<String, String> validationErrors;

    public ErrorResponse(int status, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(int status, String message, Map<String, String> validationErrors) {
        this(status, message);
        this.validationErrors = validationErrors;
    }
}