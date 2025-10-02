package com.kafka.poc.service.impl;

import com.kafka.poc.dto.TransactionDTO;
import com.kafka.poc.producer.KafkaProducer;
import com.kafka.poc.service.TransactionService;
import com.kafka.poc.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Override
    public TransactionDTO initiateTransaction(TransactionDTO transactionDTO) {
        log.info("Initiating transaction request : {}", Utility.objectToJsonString(transactionDTO));
        try{
            kafkaProducer.sendMessage("banking.transaction.topic", Utility.objectToJsonString(transactionDTO));
            return transactionDTO;
        } catch (Exception e){
            log.error("Error while initiating transaction : {}", e.getMessage());
            throw e;
        }
    }
}
