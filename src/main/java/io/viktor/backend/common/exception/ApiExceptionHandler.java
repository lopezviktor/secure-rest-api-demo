package io.viktor.backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return baseBody(HttpStatus.BAD_REQUEST, request)
                .with("message", ex.getMessage())
                .build();
    }

    // Handles DTO validation errors triggered by @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            // Keep first message per field to avoid noise
            fields.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        return baseBody(HttpStatus.BAD_REQUEST, request)
                .with("message", "Validation failed")
                .with("fields", fields)
                .build();
    }

    // Handles malformed JSON / missing body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return baseBody(HttpStatus.BAD_REQUEST, request)
                .with("message", "Malformed JSON request")
                .build();
    }

    /**
     * Minimal consistent error shape (close to Spring Boot's default, but with message/fields when needed)
     */
    private ErrorBodyBuilder baseBody(HttpStatus status, HttpServletRequest request) {
        return new ErrorBodyBuilder()
                .with("timestamp", Instant.now().toString())
                .with("status", status.value())
                .with("error", status.getReasonPhrase())
                .with("path", request.getRequestURI());
    }

    private static class ErrorBodyBuilder {
        private final Map<String, Object> map = new LinkedHashMap<>();

        ErrorBodyBuilder with(String key, Object value) {
            map.put(key, value);
            return this;
        }

        Map<String, Object> build() {
            return map;
        }
    }

    // Handles validation errors on @RequestParam / @PathVariable constraints (e.g., @Min, @Max)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(v -> {
            // Example: "getTasks.page" or "getTasks.size" depending on method/param names
            String key = (v.getPropertyPath() != null) ? v.getPropertyPath().toString() : "param";
            fields.putIfAbsent(key, v.getMessage());
        });

        return baseBody(HttpStatus.BAD_REQUEST, request)
                .with("message", "Validation failed")
                .with("fields", fields)
                .build();
    }
}