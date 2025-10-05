package com.kafka.poc.service.impl;

import com.kafka.poc.dto.CreateTopicRequestDTO;
import com.kafka.poc.exception.CommonCustomException;
import com.kafka.poc.model.TopicInfo;
import com.kafka.poc.service.TopicService;
import com.kafka.poc.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service implementation for managing Kafka topics.
 * <p>
 * This class provides methods to create, retrieve, list, and delete Kafka topics
 * using the Kafka AdminClient. It handles exceptions and logs relevant information
 * for each operation.
 * </p>
 */
@Slf4j
@Service
public class TopicServiceImpl implements TopicService {

    /**
     * KafkaAdmin instance for managing Kafka topics.
     */
    @Autowired
    private KafkaAdmin kafkaAdmin;

    /**
     * Creates a new Kafka topic based on the provided request data.
     *
     * @param createTopicRequestDTO The DTO containing topic creation details such as name, number of partitions, and replication factor.
     * @return The created TopicInfo object containing details of the newly created topic.
     * @throws CommonCustomException if the topic already exists or if there is an error during creation.
     */
    @Override
    public TopicInfo createTopic(CreateTopicRequestDTO createTopicRequestDTO) {
        log.info("Received request to create topic: {}", Utility.objectToJsonString(createTopicRequestDTO));
        TopicInfo topicInfo;
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            NewTopic newTopic;
            if (createTopicRequestDTO.getReplicasAssignments() != null && !createTopicRequestDTO.getReplicasAssignments().isEmpty()) {
                newTopic = new NewTopic(createTopicRequestDTO.getTopicName(), createTopicRequestDTO.getReplicasAssignments());
            } else {
                newTopic = new NewTopic(createTopicRequestDTO.getTopicName(),
                        createTopicRequestDTO.getPartitions() > 0 ? createTopicRequestDTO.getPartitions() : 1,
                        createTopicRequestDTO.getReplicationFactor() > 0 ? createTopicRequestDTO.getReplicationFactor() : 1);
            }
            if (createTopicRequestDTO.getConfigs() != null && !createTopicRequestDTO.getConfigs().isEmpty()) {
                newTopic.configs(createTopicRequestDTO.getConfigs());
            }
            CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));
            createTopicsResult.all().get();
            topicInfo = getTopicInfo(createTopicRequestDTO.getTopicName());
            log.info("Topic '{}' created successfully with details: {}", createTopicRequestDTO.getTopicName(), Utility.objectToJsonString(topicInfo));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof TopicExistsException) {
                log.warn("Topic '{}' already exists. Exception: {}", createTopicRequestDTO.getTopicName(), e.getMessage(), e);
                throw new CommonCustomException(
                        HttpStatus.CONFLICT.value(),
                        String.format("Topic '%s' already exists.", createTopicRequestDTO.getTopicName()));
            } else if (cause instanceof InvalidReplicationFactorException) {
                log.error("Invalid replication factor for topic '{}'. Exception: {}", createTopicRequestDTO.getTopicName(), e.getMessage(), e);
                throw new CommonCustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        String.format("Invalid replication factor for topic '%s': %s", createTopicRequestDTO.getTopicName(), cause.getMessage()));
            } else {
                log.error("Failed to create topic '{}'. Unexpected error: {}", createTopicRequestDTO.getTopicName(), e.getMessage(), e);
                throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create topic due to an unexpected error.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Topic creation for '{}' was interrupted. Error: {}", createTopicRequestDTO.getTopicName(), e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Topic creation was interrupted.");
        } catch (Exception e) {
            log.error("Unexpected error occurred while creating topic '{}': {}", createTopicRequestDTO.getTopicName(), e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while creating topic.");
        }
        return topicInfo;
    }

    /**
     * Retrieves the names of all topics present in the Kafka cluster.
     *
     * @return A set of topic names.
     * @throws CommonCustomException if there is an error while fetching topic names.
     */
    @Override
    public Set<String> getAllTopicName() {
        log.info("Fetching all topic names from the Kafka cluster.");
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> topicsName = adminClient.listTopics().names().get();
            log.info("Successfully fetched topic names: {}", topicsName);
            return topicsName;
        } catch (Exception e) {
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while creating topic.");
        }
    }

    /**
     * Retrieve metadata and configuration information for a specific Kafka topic.
     *
     * @param topicName The name of the topic to retrieve information for.
     * @return The TopicInfo object containing metadata and configuration of the specified topic, or null if not found.
     * @throws CommonCustomException if the topic does not exist or if there is an error during retrieval.
     */
    @Override
    public TopicInfo getTopicInfo(String topicName) {
        log.info("Fetching topic info for '{}'", topicName);
        TopicInfo topicInfo = null;
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topicName));
            if (describeTopicsResult != null) {
                TopicDescription topicDescription = describeTopicsResult.topicNameValues().get(topicName).get();
                ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
                DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(Collections.singleton(configResource));
                Config config = describeConfigsResult.all().get().get(configResource);

                topicInfo = TopicInfo.builder()
                        .topicName(topicDescription.name())
                        .partitions(topicDescription.partitions().size())
                        .replicationFactor((short) topicDescription.partitions().getFirst().replicas().size())
                        .replicasAssignments(topicDescription.partitions().stream()
                                .collect(Collectors.toMap(
                                        TopicPartitionInfo::partition,
                                        p -> p.replicas().stream().map(Node::id).collect(Collectors.toList())
                                )))
                        .configs(config.entries().stream()
                                .collect(Collectors.toMap(
                                        ConfigEntry::name,
                                        ConfigEntry::value
                                )))
                        .build();
                log.info("Successfully fetched topic info for '{}': {}", topicName, Utility.objectToJsonString(topicInfo));
            }
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.warn("Topic '{}' does not exist. Exception: {}", topicName, e.getMessage(), e);
                throw new CommonCustomException(
                        HttpStatus.NOT_FOUND.value(),
                        String.format("Topic '%s' does not exist.", topicName)
                );
            } else {
                log.error("Error while fetching topic info for '{}': {}", topicName, e.getMessage(), e);
                throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error occurred while fetching topic info.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Fetching topic info for '{}' was interrupted. Error: {}", topicName, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fetching topic info was interrupted.");
        } catch (Exception e) {
            log.error("Unexpected error while fetching topic info for '{}': {}", topicName, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while fetching topic info.");
        }
        return topicInfo;
    }

    /**
     * Deletes a specified Kafka topic from the cluster.
     *
     * @param topicName The name of the topic to be deleted.
     * @throws CommonCustomException if the topic does not exist or if there is an error during deletion.
     */
    @Override
    public void deleteTopic(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singleton(topicName));
            deleteTopicsResult.all().get();
            log.info("Topic '{}' deleted successfully.", topicName);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof UnknownTopicOrPartitionException) {
                log.warn("Topic '{}' does not exist. Exception: {}", topicName, e.getMessage(), e);
                throw new CommonCustomException(HttpStatus.NOT_FOUND.value(), String.format("Topic '%s' does not exist.", topicName));
            } else {
                log.error("Error while deleting topic '{}': {}", topicName, e.getMessage(), e);
                throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error occurred while deleting topic.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Deletion of topic '{}' was interrupted. Error: {}", topicName, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Topic deletion was interrupted.");
        } catch (Exception e) {
            log.error("Unexpected error while deleting topic '{}': {}", topicName, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while deleting topic.");
        }
    }
}
