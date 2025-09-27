package com.kafka.poc.model.common;

import lombok.*;

/**
 * Generic response wrapper for error API operations.
 * <p>
 * This class provides a standardized structure for error responses returned by the API.
 * It includes a timestamp, status, HTTP status code, and a message describing the error.
 * </p>
 *
 * <p>Example usage:
 * <pre>
 *     CommonErrorResponse error = CommonErrorResponse.builder()
 *         .timestamp(Instant.now().toString())
 *         .status("Failure")
 *         .statusCode(400)
 *         .message("Invalid request parameters.")
 *         .build();
 * </pre>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonErrorResponse {
    /**
     * The timestamp when the error response was generated (ISO-8601 format).
     */
    private String timestamp;
    /**
     * The status of the error response (e.g., "ERROR").
     */
    private String status;
    /**
     * The HTTP status code associated with the error response.
     */
    private int statusCode;
    /**
     * A human-readable message describing the error.
     */
    private String message;
}
