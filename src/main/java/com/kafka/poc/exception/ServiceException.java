package com.kafka.poc.exception;

/**
 * Custom exception for service layer errors in the Kafka POC application.
 * <p>
 * This exception is intended to be thrown when a service-level error occurs that should be
 * propagated to higher layers (such as controllers or global exception handlers). It extends
 * {@link RuntimeException} and supports both message-only and message-with-cause constructors.
 * </p>
 *
 * <p>Example usage:
 * <pre>
 *     throw new ServiceException("Service failed to process request");
 *     throw new ServiceException("Service failed", cause);
 * </pre>
 * </p>
 */
public class ServiceException extends RuntimeException{
    /**
     * Constructs a new ServiceException with the specified detail message.
     *
     * @param message the detail message
     */
    public ServiceException(String message) {
        super(message);
    }
    /**
     * Constructs a new ServiceException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
