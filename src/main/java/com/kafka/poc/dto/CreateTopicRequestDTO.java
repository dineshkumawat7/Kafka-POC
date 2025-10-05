package com.kafka.poc.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for creating a Kafka topic.
 * <p>
 * This DTO encapsulates the necessary information required to create a new Kafka topic,
 * including the topic name, number of partitions, replication factor, optional replica assignments,
 * and topic-level configuration properties.
 * </p>
 *
 * <p><b>Default Values:</b></p>
 * <ul>
 *     <li><b>partitions</b>: Defaults to 1 if not specified.</li>
 *     <li><b>replicationFactor</b>: Defaults to 1 if not specified.</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 *     CreateTopicRequestDTO request = CreateTopicRequestDTO.builder()
 *         .topicName("my-topic")
 *         .partitions(3) // optional, defaults to 1
 *         .replicationFactor((short) 2) // optional, defaults to 1
 *         .build();
 * </pre>
 * </p>
 *
 * <p>
 * If <code>partitions</code> or <code>replicationFactor</code> are not set via the builder, they will default to 1.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTopicRequestDTO {
    /**
     * Name of the Kafka topic to be created.
     */
    private String topicName;
    /**
     * Number of partitions for the Kafka topic.
     * <p>Defaults to 1 if not specified.</p>
     */
    private int partitions = 1;
    /**
     * Replication factor for the Kafka topic.
     * <p>Defaults to 1 if not specified.</p>
     */
    private short replicationFactor = 1;
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
