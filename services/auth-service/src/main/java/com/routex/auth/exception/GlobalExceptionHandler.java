package com.routex.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException e) {
        return error(HttpStatus.CONFLICT, e.getMessage(), "EMAIL_ALREADY_EXISTS");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCreds(InvalidCredentialsException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage(), "INVALID_CREDENTIALS");
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException e) {
        return error(HttpStatus.LOCKED, e.getMessage(), "ACCOUNT_LOCKED");
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage(), "INVALID_TOKEN");
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(AccountDisabledException e) {
        return error(HttpStatus.FORBIDDEN, e.getMessage(), "ACCOUNT_DISABLED");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage(), "USER_NOT_FOUND");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getAllErrors().forEach(err -> {
            String field = err instanceof FieldError fe ? fe.getField() : err.getObjectName();
            fieldErrors.put(field, err.getDefaultMessage());
        });
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Validation failed", "VALIDATION_ERROR",
            Instant.now(), fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred", "INTERNAL_ERROR");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(status.value(), message, code, Instant.now(), null));
    }

    public record ErrorResponse(
        int status, String message, String code,
        Instant timestamp, Map<String, String> fieldErrors
    ) {}
}
