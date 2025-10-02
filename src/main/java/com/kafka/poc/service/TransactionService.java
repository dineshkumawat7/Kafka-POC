package com.kafka.poc.service;

import com.kafka.poc.dto.TransactionDTO;

public interface TransactionService {
    TransactionDTO initiateTransaction(TransactionDTO transactionDTO);
}
