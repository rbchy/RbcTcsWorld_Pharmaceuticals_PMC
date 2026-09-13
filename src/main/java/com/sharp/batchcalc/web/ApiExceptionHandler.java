package com.sharp.batchcalc.web;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns exceptions into clean JSON error responses instead of stack traces.
 * এক্সেপশনগুলোকে পরিষ্কার JSON error response-এ রূপান্তর করে
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Thrown by allocateFromLots() when lots can't cover the requirement (shortfall). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleShortfall(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorBody(ex.getMessage()));
    }

    /** Thrown when a batch id in the URL path doesn't exist. */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage()));
    }

    /** Bean-validation failures on @Valid @RequestBody DTOs. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        StringBuilder msg = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                msg.append(fe.getField()).append(" ").append(fe.getDefaultMessage()).append("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(msg.toString()));
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("error", message);
        return body;
    }
}
