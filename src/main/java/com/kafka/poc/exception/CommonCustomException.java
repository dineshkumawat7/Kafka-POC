package com.kafka.poc.exception;

import lombok.Getter;

/**
 * Custom exception class for handling application-specific errors with status codes.
 * <p>
 * This exception is intended to be thrown when a business or application error occurs that should be
 * communicated to the client with a specific HTTP status code and message. It extends {@link RuntimeException}
 * and provides additional fields for the status code and a custom exception message.
 * </p>
 *
 * <p>Example usage:
 * <pre>
 *     throw new CommonCustomException(404, "Resource not found");
 * </pre>
 * </p>
 */
@Getter
public class CommonCustomException extends RuntimeException {
    /**
     * The HTTP status code associated with this exception.
     */
    private final int statusCode;
    /**
     * The custom exception message to be returned to the client.
     */
    private final String exceptionMessage;

    /**
     * Constructs a new CommonCustomException with the specified status code and message.
     *
     * @param statusCode the HTTP status code to be associated with this exception
     * @param exceptionMessage the custom message describing the exception
     */
    public CommonCustomException(int statusCode, String exceptionMessage) {
        super(exceptionMessage);
        this.statusCode = statusCode;
        this.exceptionMessage = exceptionMessage;
    }
}
