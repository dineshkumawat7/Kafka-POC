package com.kafka.poc.model.common;

import lombok.*;

/**
 * Generic response wrapper for successful API operations.
 * <p>
 * This class provides a standardized structure for successful responses returned by the API.
 * It includes a timestamp, status, HTTP status code, a message, and a generic payload containing
 * the actual response data.
 * </p>
 *
 * @param <T> the type of the payload returned in the response
 *
 * <p>Example usage:
 * <pre>
 *     CommonSuccessResponse<MyData> response = CommonSuccessResponse.<MyData>builder()
 *         .timestamp(Instant.now().toString())
 *         .status("SUCCESS")
 *         .statusCode(200)
 *         .message("Operation completed successfully.")
 *         .payload(myDataObject)
 *         .build();
 * </pre>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonSuccessResponse <T> {
    /**
     * The timestamp when the response was generated (ISO-8601 format).
     */
    private String timestamp;
    /**
     * The status of the response (e.g., "SUCCESS").
     */
    private String status;
    /**
     * The HTTP status code associated with the response.
     */
    private int statusCode;
    /**
     * A human-readable message describing the result.
     */
    private String message;
    /**
     * The actual payload/data returned by the API.
     */
    private T payload;
}
