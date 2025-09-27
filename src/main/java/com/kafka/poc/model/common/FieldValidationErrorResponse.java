package com.kafka.poc.model.common;

import lombok.*;

import java.util.List;

/**
 * Generic response wrapper for field validation errors in API operations.
 * <p>
 * This class provides a standardized structure for responses containing field-level validation errors.
 * It includes a timestamp, status, HTTP status code, a message, and a list of field-specific error details.
 * </p>
 *
 * @param <T> the type representing individual field error details
 *
 * <p>Example usage:
 * <pre>
 *     FieldValidationErrorResponse<MyFieldError> errorResponse = FieldValidationErrorResponse.<MyFieldError>builder()
 *         .timestamp(Instant.now().toString())
 *         .status("VALIDATION_ERROR")
 *         .statusCode(400)
 *         .message("Validation failed for one or more fields.")
 *         .fieldErrors(fieldErrorList)
 *         .build();
 * </pre>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldValidationErrorResponse <T>{
    /**
     * The timestamp when the error response was generated (ISO-8601 format).
     */
    private String timestamp;
    /**
     * The status of the error response (e.g., "VALIDATION_ERROR").
     */
    private String status;
    /**
     * The HTTP status code associated with the error response.
     */
    private int statusCode;
    /**
     * A human-readable message describing the validation error.
     */
    private String message;
    /**
     * A list of field-specific error details.
     */
    private List<T> fieldErrors;
}
