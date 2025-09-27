package com.kafka.poc.exception;

import com.kafka.poc.model.common.CommonErrorResponse;
import com.kafka.poc.utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Global exception handler for the Kafka POC application.
 * <p>
 * This class provides centralized exception handling for all controllers using Spring's {@code @RestControllerAdvice}.
 * It intercepts exceptions thrown by controller methods and returns standardized error responses
 * using the {@link CommonErrorResponse} model.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles application-specific exceptions and returns a standardized error response.
     *
     * @param e the {@link CommonCustomException} thrown by the application
     * @return a {@link ResponseEntity} containing the error response and the appropriate HTTP status code
     */
    @ExceptionHandler(CommonCustomException.class)
    public ResponseEntity<CommonErrorResponse> handleCommonCustomExceptionException(CommonCustomException e) {
        CommonErrorResponse commonErrorResponse = CommonErrorResponse.builder()
                .timestamp(String.valueOf(Instant.now()))
                .status(Constants.FAILURE_TAG)
                .statusCode(e.getStatusCode())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(e.getStatusCode()).body(commonErrorResponse);
    }

    /**
     * Handles all other unhandled exceptions and returns a generic internal server error response.
     *
     * @param e the unhandled {@link Exception}
     * @return a {@link ResponseEntity} containing the error response and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorResponse> handleException(Exception e) {
        CommonErrorResponse commonErrorResponse = CommonErrorResponse.builder()
                .timestamp(String.valueOf(Instant.now()))
                .status(Constants.FAILURE_TAG)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(commonErrorResponse);
    }
}
