package com.employee.employee_system.Exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class ErrorResponse {

    private final int status;
    private final String message;

    // Only populated for validation errors — maps field → error message
    // e.g. { "email": "must be a valid email" }
    private final Map<String, String> fieldErrors;

    private final LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(int status, String message,
                         Map<String, String> fieldErrors) {
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }
}
