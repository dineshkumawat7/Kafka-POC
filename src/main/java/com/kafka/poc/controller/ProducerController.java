package com.kafka.poc.controller;

import com.kafka.poc.exception.ServiceException;
import com.kafka.poc.model.common.CommonSuccessResponse;
import com.kafka.poc.producer.KafkaProducer;
import com.kafka.poc.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/kafka/producer")
public class ProducerController {

    @Autowired
    private KafkaProducer kafkaProducer;

    @PostMapping("/produce/{topic-name}")
    public ResponseEntity<CommonSuccessResponse<Object>> produceMessage(@PathVariable("topic-name") String topicName, @RequestBody String message) {
        kafkaProducer.sendMessage(topicName, message);
        return getSpecificResponse("Message produced successfully", HttpStatus.OK.value(), message);
    }

    @PostMapping("/produce-with-key/{topic-name}")
    public ResponseEntity<CommonSuccessResponse<Object>> produceMessageWithKey(@PathVariable("topic-name") String topicName, @RequestBody String message) {
        String key = UUID.randomUUID().toString().replace("-", "");
        kafkaProducer.sendMessageWithKey(topicName, message, key);
        String msg = String.format("Message produced successfully with key %s", key);
        return getSpecificResponse(msg, HttpStatus.OK.value(), message);
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
