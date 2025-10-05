package com.kafka.poc.controller;

import com.kafka.poc.exception.ServiceException;
import com.kafka.poc.model.ConsumerGroupInfo;
import com.kafka.poc.model.Coordinator;
import com.kafka.poc.model.MemberInfo;
import com.kafka.poc.model.OffsetInfo;
import com.kafka.poc.model.common.CommonSuccessResponse;
import com.kafka.poc.service.ConsumerGroupService;
import com.kafka.poc.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for managing Kafka consumer groups.
 * <p>
 * Provides endpoints to fetch details about consumer groups, their coordinators,
 * members, and offsets. Each endpoint returns a standardized response format
 * with user-friendly messages.
 * </p>
 */
@RestController
@RequestMapping("/api/kafka/consumer-groups")
public class ConsumerGroupController {

    /**
     * Injected ConsumerGroupService to handle business logic.
     */
    @Autowired
    private ConsumerGroupService consumerGroupService;

    /**
     * Retrieves the coordinator information for a specific consumer group.
     * <p>
     * If the coordinator is found, returns a success message with coordinator details.
     * If not found, returns a friendly message indicating no coordinator is available for the group.
     * </p>
     *
     * @param groupId The ID of the consumer group to fetch the coordinator for
     * @return ResponseEntity containing a CommonSuccessResponse with coordinator info and a user-friendly message
     */
    @GetMapping("/coordinator/{group-id}")
    public ResponseEntity<CommonSuccessResponse<Coordinator>> getCoordinator(@PathVariable("group-id") String groupId) {
        Coordinator coordinator = consumerGroupService.getCoordinator(groupId);
        String message = coordinator == null
                ? String.format("No coordinator found for consumer group '%s'.", groupId)
                : String.format("Coordinator for consumer group '%s' fetched successfully.", groupId);
        return getSpecificResponse(message, HttpStatus.OK.value(), coordinator);
    }

    /**
     * Retrieves member information for a specific consumer group.
     * <p>
     * If members are found, returns a success message with member details.
     * If no members are found, returns a friendly message indicating the absence of members for the group.
     * </p>
     *
     * @param groupId The ID of the consumer group to fetch member information for
     * @return ResponseEntity containing a CommonSuccessResponse with the list of members and a relevant message
     */
    @GetMapping("/member/{group-id}")
    public ResponseEntity<CommonSuccessResponse<List<MemberInfo>>> getMemberInfo(@PathVariable("group-id") String groupId) {
        List<MemberInfo> memberInfos = consumerGroupService.getMembers(groupId);
        String message = memberInfos.isEmpty()
                ? String.format("No member found for consumer group '%s'.", groupId)
                : String.format("Members for consumer group '%s' fetched successfully.", groupId);
        return getSpecificResponse(message, HttpStatus.OK.value(), memberInfos);
    }

    /**
     * Retrieves offset information for a specific consumer group.
     * <p>
     * If offsets are found, returns a success message with offset details.
     * If no offsets are found, returns a friendly message indicating the absence of offsets for the group.
     * </p>
     *
     * @param groupId The ID of the consumer group to fetch offset information for
     * @return ResponseEntity containing a CommonSuccessResponse with the list of offsets and a relevant message
     */
    @GetMapping("/offset/{group-id}")
    public ResponseEntity<CommonSuccessResponse<List<OffsetInfo>>> getOffsetInfo(@PathVariable("group-id") String groupId) {
        List<OffsetInfo> offsets = consumerGroupService.getOffsets(groupId);
        String message = offsets.isEmpty()
                ? String.format("No offsets found for consumer group '%s'.", groupId)
                : String.format("Offsets for consumer group '%s' fetched successfully.", groupId);
        return getSpecificResponse(message, HttpStatus.OK.value(), offsets);
    }

    /**
     * Retrieves details of all consumer groups in the Kafka cluster.
     * <p>
     * Fetches consumer group information using the ConsumerGroupService and constructs a user-friendly message
     * based on whether any consumer groups are found.
     * </p>
     *
     * @return ResponseEntity containing a CommonSuccessResponse with the list of consumer groups and a relevant message
     */
    @GetMapping
    public ResponseEntity<CommonSuccessResponse<List<ConsumerGroupInfo>>> getConsumerGroupDetails() {
        List<ConsumerGroupInfo> consumerGroups = consumerGroupService.getAllConsumerGroups();
        String message = consumerGroups.isEmpty()
                ? "No consumer groups found. Your Kafka cluster is ready for new consumers."
                : String.format("Successfully fetched %d consumer groups.", consumerGroups.size());
        return getSpecificResponse(message, HttpStatus.OK.value(), consumerGroups);
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
            throw new ServiceException("Oops! Something went wrong while preparing your response. Please try again later.", e);
        }
    }
}
