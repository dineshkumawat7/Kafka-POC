package com.kafka.poc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Utility class providing helper methods for JSON processing and other common tasks.
 * <p>
 * This class includes static methods that can be used across the application for converting objects
 * to JSON strings and potentially other utility functions in the future.
 * </p>
 */
public class Utility {
    /**
     * Private constructor to prevent instantiation.
     */
    private Utility() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converts a given object to its JSON string representation.
     *
     * @param object the object to be converted to JSON
     * @return the JSON string representation of the object, or null if the input is null
     * @throws IllegalArgumentException if the object cannot be converted to JSON
     */
    public static String objectToJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert object to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Static ObjectMapper instance for efficiency.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .enable(SerializationFeature.INDENT_OUTPUT);
}
