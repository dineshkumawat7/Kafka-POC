package com.kafka.poc.controller;

import com.kafka.poc.exception.ServiceException;
import com.kafka.poc.model.BrokerConfigs;
import com.kafka.poc.model.BrokerInfo;
import com.kafka.poc.model.BrokerLogs;
import com.kafka.poc.model.common.CommonSuccessResponse;
import com.kafka.poc.service.BrokerService;
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

@RestController
@RequestMapping("/api/kafka/broker")
public class BrokerController {

    /**
     * Service for Kafka broker management operations.
     */
    @Autowired
    private BrokerService brokerService;

    /**
     * Retrieves metadata and configuration information for all Kafka brokers in the cluster.
     *
     * @return HTTP 200 with a list of all brokers' metadata and configuration
     */
    @GetMapping("/info")
    public ResponseEntity<CommonSuccessResponse<List<BrokerInfo>>> getTopicInfo() {
        List<BrokerInfo> brokerInfos = brokerService.getAllBrokers();
        return getSpecificResponse("Fetched broker info successfully.", HttpStatus.OK.value(), brokerInfos);
    }

    @GetMapping("/{broker-id}/configs")
    public ResponseEntity<CommonSuccessResponse<List<BrokerConfigs>>> getBrokerConfig(@PathVariable("broker-id") int brokerId) {
        List<BrokerConfigs> brokerConfigs = brokerService.getBrokerConfig(brokerId);
        return getSpecificResponse("Fetched broker configs successfully.", HttpStatus.OK.value(), brokerConfigs);
    }

    @GetMapping("/{broker-id}/logs")
    public ResponseEntity<CommonSuccessResponse<List<BrokerLogs>>> getBrokerLogs(@PathVariable("broker-id") int brokerId) {
        List<BrokerLogs> brokerConfigs = brokerService.getBrokerLogs(brokerId);
        return getSpecificResponse("Fetched broker logs successfully.", HttpStatus.OK.value(), brokerConfigs);
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
