package com.kafka.poc.model;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Model representing detailed information about a Kafka topic.
 * <p>
 * This class encapsulates the metadata and configuration details of a Kafka topic, including its name,
 * number of partitions, replication factor, partition leader assignments, replica assignments, and topic-level configuration properties.
 * </p>
 *
 * <p>Example usage:
 * <pre>
 *     KafkaTopicInfo info = KafkaTopicInfo.builder()
 *         .topicName("my-topic")
 *         .partitions(3)
 *         .replicationFactor((short) 2)
 *         .replicasAssignments(Map.of(0, List.of(1,2), 1, List.of(2,3)))
 *         .configs(Map.of("retention.ms", "604800000"))
 *         .build();
 * </pre>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicInfo {
    /**
     * Name of the Kafka topic.
     */
    private String topicName;
    /**
     * Number of partitions in the topic.
     */
    private int partitions;
    /**
     * Replication factor for the topic.
     */
    private short replicationFactor;
    /**
     * Map of partition to list of broker IDs where replicas are assigned.
     * Key: partition number, Value: list of broker IDs.
     */
    private Map<Integer, List<Integer>> replicasAssignments;
    /**
     * Map of topic configuration properties (key-value pairs).
     * Key: config name, Value: config value.
     */
    private Map<String, String> configs;
}
