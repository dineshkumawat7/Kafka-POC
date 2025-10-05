package com.kafka.poc.service;

import com.kafka.poc.dto.CreateTopicRequestDTO;
import com.kafka.poc.model.TopicInfo;

import java.util.Set;

/**
 * Service interface for managing Kafka topics.
 */
public interface TopicService {
    /**
     * Create a new Kafka topic based on the provided request data.
     *
     * @param createTopicRequestDTO The DTO containing topic creation details such as name, number of partitions, and replication factor.
     * @return The created TopicInfo object containing details of the newly created topic.
     */
    TopicInfo createTopic(CreateTopicRequestDTO createTopicRequestDTO);

    /**
     * Retrieve the names of all topics present in the Kafka cluster.
     *
     * @return A set of topic names.
     */
    Set<String> getAllTopicName();

    /**
     * Retrieve metadata and configuration information for a specific Kafka topic.
     *
     * @param topicName The name of the topic to retrieve information for.
     * @return The TopicInfo object containing metadata and configuration of the specified topic, or null if not found.
     */
    TopicInfo getTopicInfo(String topicName);

    /**
     * Delete a specified Kafka topic from the cluster.
     *
     * @param topicName The name of the topic to be deleted.
     */
    void deleteTopic(String topicName);
}
