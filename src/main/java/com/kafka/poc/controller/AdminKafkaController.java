package com.kafka.poc.controller;

import com.kafka.poc.exception.ServiceException;
import com.kafka.poc.model.common.CommonSuccessResponse;
import com.kafka.poc.dto.CreateTopicRequestDTO;
import com.kafka.poc.model.KafkaTopicInfo;
import com.kafka.poc.service.AdminKafkaService;
import com.kafka.poc.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller for Kafka administration operations.
 * <p>
 * This controller exposes endpoints for health checks, topic creation, topic information retrieval,
 * and topic deletion. It delegates business logic to the {@link AdminKafkaService} and returns standardized
 * API responses using the {@link CommonSuccessResponse} model.
 * </p>
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li><b>GET /api/kafka/health</b>: Health check endpoint.</li>
 *   <li><b>POST /api/kafka/topic</b>: Create a new Kafka topic.</li>
 *   <li><b>GET /api/kafka/topic/info/{topicName}</b>: Retrieve topic metadata and configuration.</li>
 *   <li><b>DELETE /api/kafka/topic/delete/{topicName}</b>: Delete a Kafka topic.</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 *     // Create a topic
 *     POST /api/kafka/topic
 *     // Get topic info
 *     GET /api/kafka/topic/info/my-topic
 *     // Delete a topic
 *     DELETE /api/kafka/topic/delete/my-topic
 *     </pre>
 * </p>
 */
@RestController
@RequestMapping("/api/kafka")
public class AdminKafkaController {

    /**
     * Service for Kafka administration operations.
     */
    @Autowired
    private AdminKafkaService adminKafkaService;

    /**
     * Health check endpoint to verify the service is running.
     *
     * @return HTTP 200 with a status message if the service is healthy
     */
    @GetMapping("/health")
    public ResponseEntity<Object> healthCheck() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "Running");
        return ResponseEntity.status(HttpStatus.OK).body(status);
    }

    /**
     * Creates a new Kafka topic with the specified configuration.
     *
     * @param createTopicRequestDTO the DTO containing topic name, partitions, and replication factor
     * @return HTTP 201 with the created topic's metadata and configuration
     */
    @PostMapping("/topic")
    public ResponseEntity<CommonSuccessResponse<KafkaTopicInfo>> createNewKafkaTopic(@RequestBody CreateTopicRequestDTO createTopicRequestDTO) {
        KafkaTopicInfo kafkaTopicInfo = adminKafkaService.createTopic(createTopicRequestDTO);
        return getSpecificResponse("Topic created successfully", HttpStatus.CREATED.value(), kafkaTopicInfo);
    }

    /**
     * Retrieves metadata and configuration information for a given Kafka topic.
     *
     * @param topicName the name of the topic to retrieve
     * @return HTTP 200 with the topic's metadata and configuration
     */
    @GetMapping("/topic/info/{topicName}")
    public ResponseEntity<CommonSuccessResponse<KafkaTopicInfo>> getTopicInfo(@PathVariable("topicName") String topicName) {
        KafkaTopicInfo kafkaTopicInfoResponse = adminKafkaService.getTopicInfo(topicName);
        return getSpecificResponse("Fetched topic info successfully", HttpStatus.OK.value(), kafkaTopicInfoResponse);
    }

    /**
     * Deletes the specified Kafka topic from the cluster.
     *
     * @param topicName the name of the topic to delete
     * @return HTTP 200 with a success message if the topic was deleted
     */
    @DeleteMapping("/topic/delete/{topicName}")
    public ResponseEntity<CommonSuccessResponse<Object>> deleteKafkaTopic(@PathVariable("topicName") String topicName) {
        adminKafkaService.deleteTopic(topicName);
        return getSpecificResponse("Topic deleted successfully", HttpStatus.OK.value(), null);
    }

    /**
     * Helper method to build a standardized API response.
     *
     * @param msg the message to include in the response
     * @param statusCode the HTTP status code
     * @param payload the response payload
     * @return a {@link ResponseEntity} containing the standardized response
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
            throw new ServiceException("Something wrong on server", e);
        }
    }
}
