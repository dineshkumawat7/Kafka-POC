package com.kafka.poc.producer;

import com.kafka.poc.exception.CommonCustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for producing messages to Kafka topics.
 * <p>
 * This service provides methods to send messages to specified Kafka topics using the {@link KafkaTemplate}.
 * It handles asynchronous message sending and logs the success or failure of message publishing.
 * In case of failure, it throws a {@link CommonCustomException} with an appropriate HTTP status code.
 * </p>
 */
@Slf4j
@Service
public class KafkaProducer {
    /**
     * KafkaTemplate for sending messages to Kafka topics.
     */
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Publishes a message to the specified Kafka topic.
     * <p>
     * This method sends a message to the given Kafka topic asynchronously. If the message is published successfully,
     * a success log is recorded. If publishing fails, an error log is recorded and a CommonCustomException is thrown.
     *
     * @param topic   the name of the Kafka topic to which the message will be published
     * @param message the message content to be published
     * @throws CommonCustomException if message publishing fails
     */
    public void sendMessage(String topic, String message) {
        log.info("Publishing message to topic {}: {}", topic, message);
        try {
            CompletableFuture<SendResult<String, String>> completableFuture = kafkaTemplate.send(topic, message);
            completableFuture.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message published successfully to topic {}: {}", topic, message);
                } else {
                    log.error("Failed to publish message to topic {}: {}", topic, ex.getMessage());
                    throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to publish message to topic " + topic);
                }
            });
        } catch (Exception e) {
            log.error("Failed to publish message to topic {}: {}", topic, e.getMessage());
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to publish message to topic " + topic);
        }
    }

    /**
     * Publishes a message with a specific key to the specified Kafka topic.
     * <p>
     * This method sends a message with a key to the given Kafka topic asynchronously. If the message is published successfully,
     * a success log is recorded. If publishing fails, an error log is recorded and a CommonCustomException is thrown.
     *
     * @param topic   the name of the Kafka topic to which the message will be published
     * @param message the message content to be published
     * @param key     the key associated with the message
     * @throws CommonCustomException if message publishing fails
     */
    public void sendMessageWithKey(String topic, String message, String key) {
        log.info("Publishing message to topic {} with key {}: {}", topic, key, message);
        try {
            CompletableFuture<SendResult<String, String>> completableFuture = kafkaTemplate.send(topic, key, message);
            completableFuture.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message published successfully to topic {} with key {}: {}", topic, key, message);
                } else {
                    log.error("Failed to publish message to topic {} with key {}: {}", topic, key, ex.getMessage());
                    throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to publish message to topic " + topic);
                }
            });
        } catch (Exception e) {
            log.error("Failed to publish message to topic {} with key {}: {}", topic, key, e.getMessage());
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to publish message to topic " + topic);
        }
    }
}
