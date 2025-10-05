package com.kafka.poc.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionConsumer {

    @KafkaListener(topics = "banking.transaction.topic", groupId = "transaction_group")
    public void consumeTransaction(String message) {
        log.info("Consumed transaction message: {}", message);
    }

    @KafkaListener(topics = "test.1", groupId = "test_group")
    public void consumeTest1(String message) {
        log.info("Consumed test-1 message: {}", message);
    }

    @KafkaListener(topics = "test.2", groupId = "test_group")
    public void consumeTest2(String message) {
        log.info("Consumed test-2 message: {}", message);
    }
}
