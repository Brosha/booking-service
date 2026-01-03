package org.example.common.exception;

import org.example.common.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDTO> handleIllegalState(IllegalStateException ex) {
        return buildResponse("CONFLICT", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDTO> handleAuthenticationException(AuthenticationException ex) {
        return buildResponse("UNAUTHORIZED", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse("FORBIDDEN", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDTO> handleRuntimeException(RuntimeException ex) {
        return buildResponse("RUNTIME_ERROR", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorDTO> buildResponse(String errorCode, String message, HttpStatus status) {
        String correlationId = UUID.randomUUID().toString();
        ErrorDTO error = ErrorDTO.builder()
                .error(errorCode)
                .message(message)
                .correlationId(correlationId)
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
