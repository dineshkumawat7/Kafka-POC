package com.kafka.poc.controller;

import com.kafka.poc.dto.CreateTopicRequestDTO;
import com.kafka.poc.exception.ServiceException;
import com.kafka.poc.model.TopicInfo;
import com.kafka.poc.model.common.CommonSuccessResponse;
import com.kafka.poc.service.TopicService;
import com.kafka.poc.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Set;

/**
 * REST controller for Kafka topic management operations.
 * <p>
 * This controller exposes endpoints for creating, retrieving, listing, and deleting Kafka topics.
 * It delegates business logic to the {@link TopicService} and returns standardized
 * API responses using the {@link CommonSuccessResponse} model.
 * </p>
 */
@RestController
@RequestMapping("/api/kafka/topic")
public class TopicController {

    /**
     * Service for Kafka topic management operations.
     */
    @Autowired
    private TopicService topicService;

    /**
     * Creates a new Kafka topic with the specified configuration.
     *
     * @param createTopicRequestDTO the DTO containing topic name, partitions, and replication factor
     * @return HTTP 201 with the created topic's metadata and configuration
     */
    @PostMapping("/create")
    public ResponseEntity<CommonSuccessResponse<TopicInfo>> createNewKafkaTopic(@RequestBody CreateTopicRequestDTO createTopicRequestDTO) {
        TopicInfo topicInfo = topicService.createTopic(createTopicRequestDTO);
        return getSpecificResponse("Topic created successfully.", HttpStatus.CREATED.value(), topicInfo);
    }

    /**
     * Retrieves metadata and configuration information for a given Kafka topic.
     *
     * @param topicName the name of the topic to retrieve
     * @return HTTP 200 with the topic's metadata and configuration, or 404 if not found
     */
    @GetMapping("/info/{topicName}")
    public ResponseEntity<CommonSuccessResponse<TopicInfo>> getTopicInfo(@PathVariable("topicName") String topicName) {
        TopicInfo topicInfoResponse = topicService.getTopicInfo(topicName);
        return getSpecificResponse("Fetched topic info successfully.", HttpStatus.OK.value(), topicInfoResponse);
    }

    /**
     * Retrieves the names of all topics present in the Kafka cluster.
     *
     * @return HTTP 200 with a set of topic names
     */
    @GetMapping
    public ResponseEntity<CommonSuccessResponse<Set<String>>> getAllTopicDetails() {
        Set<String> topicName = topicService.getAllTopicName();
        return getSpecificResponse("Topic list fetched successfully.", HttpStatus.OK.value(), topicName);
    }

    /**
     * Deletes the specified Kafka topic from the cluster.
     *
     * @param topicName the name of the topic to delete
     * @return HTTP 200 with a success message
     */
    @DeleteMapping("/delete/{topicName}")
    public ResponseEntity<CommonSuccessResponse<Object>> deleteKafkaTopic(@PathVariable("topicName") String topicName) {
        topicService.deleteTopic(topicName);
        return getSpecificResponse("Topic deleted successfully.", HttpStatus.OK.value(), null);
    }

    /**
     * Constructs a standardized API response with the given parameters.
     *
     * @param msg        the message to include in the response
     * @param statusCode the HTTP status code for the response
     * @param payload    the payload data to include in the response
     * @param <T>        the type of the payload
     * @return a {@link ResponseEntity} containing a {@link CommonSuccessResponse} with the specified details
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
