package com.employee.employee_system.Exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — employee not found
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            EmployeeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage(), null));
    }

    // 409 — duplicate email
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateEmailException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage(), null));
    }

    // 400 — @Valid on RequestDto failed
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                errs.put(fe.getField(), fe.getDefaultMessage())
        );
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(400, "Validation failed", errs));
    }

    // 400 — @Validated on Service (ConstraintViolationException)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(
            ConstraintViolationException ex) {
        Map<String, String> errs = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                errs.put(cv.getPropertyPath().toString(), cv.getMessage())
        );
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(400, "Constraint violation", errs));
    }

    // 400 — wrong file type uploaded
    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<ErrorResponse> handleFileFormat(
            InvalidFileFormatException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(400, ex.getMessage(), null));
    }

    // 500 — catch-all safety net
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500,
                        "An unexpected error occurred", null));
    }
}