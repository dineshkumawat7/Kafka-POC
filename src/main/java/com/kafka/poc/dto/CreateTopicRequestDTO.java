package com.kafka.poc.dto;

import lombok.*;

/**
 * Data Transfer Object for creating a Kafka topic.
 * <p>
 * This DTO encapsulates the necessary information required to create a new Kafka topic,
 * including the topic name, number of partitions, and replication factor.
 * </p>
 *
 * <p>Example usage:
 * <pre>
 *     CreateTopicRequestDTO request = CreateTopicRequestDTO.builder()
 *         .topicName("my-topic")
 *         .partitions(3)
 *         .replicationFactor((short) 2)
 *         .build();
 * </pre>
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
     */
    private int partitions;
    /**
     * Replication factor for the Kafka topic.
     */
    private short replicationFactor;
}
