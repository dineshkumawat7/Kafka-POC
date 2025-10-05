package com.kafka.poc.controller;

import com.kafka.poc.exception.ServiceException;
import com.kafka.poc.model.common.CommonSuccessResponse;
import com.kafka.poc.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * REST controller for health check endpoint.
 * <p>
 * Provides an API to check the health status of the application and its dependencies.
 * </p>
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Injected Spring Boot HealthEndpoint to retrieve health status.
     */
    @Autowired
    private HealthEndpoint healthEndpoint;

    /**
     * Performs a health check of the application and returns a user-friendly message along with health details.
     * <p>
     * The response includes a message based on the health status (UP, DOWN, OUT_OF_SERVICE, UNKNOWN) and the full health details.
     * </p>
     *
     * @return ResponseEntity containing a CommonSuccessResponse with health status and details
     */
    @GetMapping
    public ResponseEntity<CommonSuccessResponse<Object>> healthCheck() {
        String status = healthEndpoint.health().getStatus().getCode();
        String message = switch (status) {
            case "UP" -> "Service is up and running. All systems are healthy.";
            case "DOWN" -> "Service is currently down. Please contact support.";
            case "OUT_OF_SERVICE" -> "Service is out of service. Maintenance may be in progress.";
            case "UNKNOWN" -> "Service health is unknown. Please try again later.";
            default -> "Service status: " + status;
        };
        return getSpecificResponse(message, HttpStatus.OK.value(), healthEndpoint.health());
    }

    /**
     * Constructs a standardized API response for successful operations.
     * <p>
     * Builds a CommonSuccessResponse object with timestamp, status, status code, message, and payload.
     * </p>
     *
     * @param msg        User-friendly message to include in the response
     * @param statusCode HTTP status code for the response
     * @param payload    Data payload to include in the response
     * @param <T>        Type of the payload
     * @return ResponseEntity containing the CommonSuccessResponse
     * @throws ServiceException if an error occurs while building the response
     */
    private <T> ResponseEntity<CommonSuccessResponse<T>> getSpecificResponse(String msg, int statusCode, T payload) {
        try {
            CommonSuccessResponse<T> response = CommonSuccessResponse.<T>builder()
                    .timestamp((Instant.now().toString()))
                    .status(Constants.SUCCESS_TAG)
                    .statusCode(statusCode)
                    .message(msg)
                    .payload(payload)
                    .build();
            return ResponseEntity.status(statusCode).body(response);
        } catch (Exception e) {
            throw new ServiceException("Something wrong on server.", e);
        }
    }
}
