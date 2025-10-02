package com.kafka.poc.controller;

import com.kafka.poc.dto.TransactionDTO;
import com.kafka.poc.exception.ServiceException;
import com.kafka.poc.model.common.CommonSuccessResponse;
import com.kafka.poc.service.TransactionService;
import com.kafka.poc.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/initiate")
    public ResponseEntity<CommonSuccessResponse<TransactionDTO>> initiateTransaction(@RequestBody TransactionDTO transactionDTO) {
        TransactionDTO transaction = transactionService.initiateTransaction(transactionDTO);
        return getSpecificResponse("Transaction initiate successfully", HttpStatus.OK.value(), transaction);
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
