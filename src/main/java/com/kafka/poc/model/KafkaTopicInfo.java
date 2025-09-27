package com.kafka.poc.model;

import lombok.*;

import java.util.Map;

/**
 * Model representing detailed information about a Kafka topic.
 * <p>
 * This class encapsulates the metadata and configuration details of a Kafka topic, including its name,
 * number of partitions, replication factor, partition leaders, and topic-level configuration properties.
 * </p>
 *
 * <p>Example usage:
 * <pre>
 *     KafkaTopicInfo info = KafkaTopicInfo.builder()
 *         .topicName("my-topic")
 *         .partitions(3)
 *         .replicationFactor((short) 2)
 *         .partitionLeaders(Map.of(0, "broker1", 1, "broker2"))
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
public class KafkaTopicInfo {
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
     * Map of partition numbers to their leader broker hostnames.
     */
    private Map<Integer, String> partitionLeaders;
    /**
     * Map of topic configuration properties (key-value pairs).
     */
    private Map<String, Object> configs;
}
