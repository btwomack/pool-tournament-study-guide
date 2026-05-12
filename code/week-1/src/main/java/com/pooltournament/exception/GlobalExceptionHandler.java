package com.pooltournament.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TournamentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(TournamentNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(UnauthorizedAccessException ex) {
        return ResponseEntity.status(403).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidMatchResultException.class)
    public ResponseEntity<Map<String, String>> handleBadResult(InvalidMatchResultException ex) {
        return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.status(400).body(Map.of(
                "error", "Validation failed",
                "details", errors));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCreds(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity.status(500).body(Map.of("error", "An unexpected error occurred"));
    }
}
