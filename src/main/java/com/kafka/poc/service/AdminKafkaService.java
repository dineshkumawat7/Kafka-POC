package com.kafka.poc.service;

import com.kafka.poc.dto.CreateTopicRequestDTO;
import com.kafka.poc.model.KafkaTopicInfo;

/**
 * Service interface for Kafka administration operations.
 * <p>
 * This interface defines methods for creating, retrieving, and deleting Kafka topics.
 * Implementations of this interface should provide the logic to interact with the Kafka cluster
 * for topic management and metadata retrieval.
 * </p>
 */
public interface AdminKafkaService {
    /**
     * Creates a new Kafka topic with the specified configuration.
     *
     * @param createTopicRequestDTO the DTO containing topic name, partitions, and replication factor
     * @return {@link KafkaTopicInfo} containing metadata and configuration of the created or existing topic
     */
    KafkaTopicInfo createTopic(CreateTopicRequestDTO createTopicRequestDTO);

    /**
     * Retrieves metadata and configuration information for a given Kafka topic.
     *
     * @param topicName the name of the topic to retrieve
     * @return {@link KafkaTopicInfo} containing topic metadata and configuration, or null if not found
     */
    KafkaTopicInfo getTopicInfo(String topicName);

    /**
     * Deletes the specified Kafka topic from the cluster.
     *
     * @param topicName the name of the topic to delete
     */
    void deleteTopic(String topicName);
}
