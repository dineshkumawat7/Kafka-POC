package com.kafka.poc.service.impl;

import com.kafka.poc.dto.CreateTopicRequestDTO;
import com.kafka.poc.exception.CommonCustomException;
import com.kafka.poc.model.KafkaTopicInfo;
import com.kafka.poc.service.AdminKafkaService;
import com.kafka.poc.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of {@link AdminKafkaService} for Kafka administration operations.
 * <p>
 * This service provides methods to create, retrieve, and delete Kafka topics using the Kafka AdminClient API.
 * It handles business exceptions and translates them into custom exceptions with appropriate HTTP status codes.
 * </p>
 */
@Service
@Slf4j
public class AdminKafkaServiceImpl implements AdminKafkaService {

    /**
     * KafkaAdmin bean for accessing Kafka cluster configuration.
     */
    @Autowired
    private KafkaAdmin kafkaAdmin;

    /**
     * Creates a new Kafka topic with the specified configuration.
     * Throws a conflict exception if the topic already exists.
     *
     * @param createTopicRequestDTO the DTO containing topic name, partitions, and replication factor
     * @return {@link KafkaTopicInfo} containing metadata and configuration of the created or existing topic
     * @throws CommonCustomException if the topic already exists or another error occurs
     */
    @Override
    public KafkaTopicInfo createTopic(CreateTopicRequestDTO createTopicRequestDTO) {
        log.info("Incoming topic creation request : {}", Utility.objectToJsonString(createTopicRequestDTO));
        KafkaTopicInfo kafkaTopicInfo = null;
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            NewTopic newTopic = new NewTopic(
                    createTopicRequestDTO.getTopicName(),
                    createTopicRequestDTO.getPartitions(),
                    createTopicRequestDTO.getReplicationFactor());
            CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));
            try {
                createTopicsResult.all().get();
                kafkaTopicInfo = getTopicInfo(createTopicRequestDTO.getTopicName());
                log.info("Topic created successfully : {}", Utility.objectToJsonString(kafkaTopicInfo));
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof TopicExistsException) {
                    log.warn("Topic already exists: {}", createTopicRequestDTO.getTopicName());
                    throw new CommonCustomException(HttpStatus.CONFLICT.value(), cause.getMessage());
                } else {
                    log.error("An error occurred while topic creation : {}", e.getMessage());
                    throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Topic creation was interrupted : {}", e.getMessage());
                throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("An error occurred while topic creation : {}", e.getMessage());
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return kafkaTopicInfo;
    }

    /**
     * Retrieves metadata and configuration information for a given Kafka topic.
     * Throws a not found exception if the topic does not exist.
     *
     * @param topicName the name of the topic to retrieve
     * @return {@link KafkaTopicInfo} containing topic metadata and configuration
     * @throws CommonCustomException if the topic does not exist or another error occurs
     */
    @Override
    public KafkaTopicInfo getTopicInfo(String topicName) {
        KafkaTopicInfo kafkaTopicInfo = null;
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topicName));
            if (describeTopicsResult != null) {
                TopicDescription topicDescription = describeTopicsResult.topicNameValues().get(topicName).get();
                ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
                DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(Collections.singleton(configResource));
                Config config = describeConfigsResult.all().get().get(configResource);

                kafkaTopicInfo = KafkaTopicInfo.builder()
                        .topicName(topicDescription.name())
                        .partitions(topicDescription.partitions().size())
                        .replicationFactor((short) topicDescription.partitions().getFirst().replicas().size())
                        .partitionLeaders(topicDescription.partitions().stream()
                                .collect(
                                        java.util.stream.Collectors.toMap(
                                                TopicPartitionInfo::partition,
                                                topic -> topic.leader() != null ? topic.leader().host() : "none"
                                        )
                                )
                        )
                        .configs(
                                config.entries().stream()
                                        .collect(java.util.stream.Collectors.toMap(
                                                ConfigEntry::name,
                                                ConfigEntry::value
                                        ))
                        )
                        .build();
                log.info("Fetched topic info successfully : {}", Utility.objectToJsonString(kafkaTopicInfo));
                return kafkaTopicInfo;
            }
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.warn("Topic does not exist : {}", topicName);
                throw new CommonCustomException(HttpStatus.NOT_FOUND.value(), e.getMessage());
            } else {
                log.error("An error occurred while deleting topic : {}", e.getMessage());
                throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Topic fetch was interrupted : {}", e.getMessage());
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while fetching topic info : {}", e.getMessage());
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return kafkaTopicInfo;
    }

    /**
     * Deletes the specified Kafka topic from the cluster.
     * Throws a not found exception if the topic does not exist.
     *
     * @param topicName the name of the topic to delete
     * @throws CommonCustomException if the topic does not exist or another error occurs
     */
    @Override
    public void deleteTopic(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singleton(topicName));
            try {
                deleteTopicsResult.all().get();
                log.info("Topic deleted successfully : {}", topicName);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof UnknownTopicOrPartitionException) {
                    log.warn("Topic does not exist : {}", topicName);
                    throw new CommonCustomException(HttpStatus.NOT_FOUND.value(), e.getMessage());
                } else {
                    log.error("An error occurred while deleting topic : {}", e.getMessage());
                    throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Topic deletion was interrupted : {}", e.getMessage());
                throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("An error occurred while deleting topic : {}", e.getMessage());
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }
}
