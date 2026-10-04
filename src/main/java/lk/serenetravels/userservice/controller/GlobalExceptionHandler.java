package lk.serenetravels.userservice.controller;

import lk.serenetravels.userservice.exception.AuthenticationFailedException;
import lk.serenetravels.userservice.exception.InvalidManagerAssignmentException;
import lk.serenetravels.userservice.exception.KeycloakAdminException;
import lk.serenetravels.userservice.exception.UserAlreadyExistsException;
import lk.serenetravels.userservice.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyExists(UserAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(UserNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(KeycloakAdminException.class)
    public ResponseEntity<Map<String, Object>> handleKeycloakAdmin(KeycloakAdminException ex) {
        return problem(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(InvalidManagerAssignmentException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidManagerAssignment(InvalidManagerAssignmentException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> problem(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message));
    }
}
