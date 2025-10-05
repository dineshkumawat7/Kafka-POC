package com.kafka.poc.service.impl;

import com.kafka.poc.exception.CommonCustomException;
import com.kafka.poc.model.ConsumerGroupInfo;
import com.kafka.poc.model.Coordinator;
import com.kafka.poc.model.MemberInfo;
import com.kafka.poc.model.OffsetInfo;
import com.kafka.poc.props.KafkaConsumerProps;
import com.kafka.poc.service.ConsumerGroupService;
import com.kafka.poc.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Service implementation for managing Kafka consumer groups.
 * <p>
 * This service provides methods to fetch information about consumer groups,
 * including their coordinators, members, and offsets. It interacts with the Kafka
 * cluster using the AdminClient and KafkaConsumer APIs.
 * </p>
 */
@Slf4j
@Service
public class ConsumerGroupServiceImpl implements ConsumerGroupService {

    /**
     * Injected KafkaAdmin to interact with Kafka cluster.
     */
    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Autowired
    private KafkaConsumerProps kafkaConsumerProps;

    /**
     * Fetches all consumer groups in the Kafka cluster.
     *
     * @return a list of ConsumerGroupInfo objects representing all consumer groups
     * @throws CommonCustomException if there is an error while fetching consumer groups
     */
    @Override
    public List<ConsumerGroupInfo> getAllConsumerGroups() {
        List<ConsumerGroupInfo> consumerGroupInfos = new ArrayList<>();
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            log.info("Initiating retrieval of all consumer groups from Kafka cluster...");
            ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.all().get(30, TimeUnit.SECONDS);
            if (consumerGroupListings.isEmpty()) {
                log.warn("No consumer groups found. Your Kafka cluster is ready for new consumers!");
                return consumerGroupInfos;
            }
            consumerGroupListings.forEach(consumerGroupListing -> {
                DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Collections.singleton(consumerGroupListing.groupId()));
                try {
                    ConsumerGroupDescription consumerGroupDescription = describeConsumerGroupsResult.describedGroups().get(consumerGroupListing.groupId()).get();
                    Coordinator coordinator = getCoordinator(consumerGroupListing.groupId());
                    List<MemberInfo> members = getMembers(consumerGroupListing.groupId());
                    List<OffsetInfo> offsets = getOffsets(consumerGroupListing.groupId());
                    ConsumerGroupInfo consumerGroupInfo = ConsumerGroupInfo.builder()
                            .id(consumerGroupListing.groupId())
                            .state(consumerGroupListing.state().isPresent() ? consumerGroupListing.state().get().toString() : "UNKNOWN")
                            .isSimpleConsumerGroup(consumerGroupListing.isSimpleConsumerGroup())
                            .type(consumerGroupListing.type().isPresent() ? consumerGroupListing.type().get().toString() : "UNKNOWN")
                            .partitionAssignor(consumerGroupDescription.partitionAssignor())
                            .coordinator(coordinator)
                            .members(members)
                            .offsets(offsets)
                            .topics(consumerGroupDescription.members().stream().flatMap(member ->
                                    member.assignment().topicPartitions().stream().map(TopicPartition::topic)).distinct().toList())
                            .activeTopics(offsets.stream().map(OffsetInfo::getTopic).distinct().toList())
                            .authorizedOperations(consumerGroupDescription.authorizedOperations())
                            .build();
                    consumerGroupInfos.add(consumerGroupInfo);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error while processing consumer group '{}': {}", consumerGroupListing.groupId(), e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            log.error("Failed to fetch consumer groups from Kafka cluster. Reason: {}", cause.getMessage(), cause);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to fetch consumer groups at this time. Please try again later.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Consumer group retrieval interrupted. Reason: {}", e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Consumer group retrieval was interrupted. Please try again.");
        } catch (Exception e) {
            log.error("Unexpected error during consumer group retrieval: {}", e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while fetching consumer groups. Please contact support.");
        }
        log.info("Successfully fetched {} consumer groups from Kafka cluster. {}", consumerGroupInfos.size(), Utility.objectToJsonString(consumerGroupInfos));
        return consumerGroupInfos;
    }

    /**
     * Fetches the coordinator information for a given consumer group.
     *
     * @param groupId the ID of the consumer group
     * @return a Coordinator object representing the coordinator of the specified consumer group
     * @throws CommonCustomException if the consumer group is not found or if there is an error while fetching the coordinator
     */
    @Override
    public Coordinator getCoordinator(String groupId) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            log.info("Fetching coordinator for consumer group '{}'.", groupId);
            DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Collections.singleton(groupId));
            ConsumerGroupDescription consumerGroupDescription = describeConsumerGroupsResult.describedGroups().get(groupId).get();
            Node node = consumerGroupDescription.coordinator();
            Coordinator coordinator = Coordinator.builder()
                    .id(node.id())
                    .idString(node.idString())
                    .host(node.host())
                    .port(node.port())
                    .rack(node.rack())
                    .build();
            log.info("Coordinator for group '{}' fetched successfully: {}", groupId, Utility.objectToJsonString(coordinator));
            return coordinator;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof GroupIdNotFoundException) {
                log.warn("Consumer group '{}' not found. Please check the group ID and try again.", groupId);
                throw new CommonCustomException(HttpStatus.NOT_FOUND.value(), "Consumer group with id '" + groupId + "' not found. Please verify the group ID.");
            } else {
                log.error("Error while fetching coordinator for group '{}': {}", groupId, e.getMessage(), e);
                throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to fetch coordinator for group '" + groupId + "'. Please try again later.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("⏸Coordinator fetch for group '{}' was interrupted. Reason: {}", groupId, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Coordinator fetch was interrupted. Please try again.");
        } catch (Exception e) {
            log.error("Unexpected error while fetching coordinator for group '{}': {}", groupId, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while fetching coordinator. Please contact support.");
        }
    }

    /**
     * Fetches the member information for a given consumer group.
     *
     * @param groupId the ID of the consumer group
     * @return a list of MemberInfo objects representing the members of the specified consumer group
     * @throws CommonCustomException if the consumer group is not found or if there is an error while fetching members
     */
    @Override
    public List<MemberInfo> getMembers(String groupId) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            log.info("Fetching member information for consumer group '{}'.", groupId);
            List<MemberInfo> memberInfos = new ArrayList<>();
            DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Collections.singleton(groupId));
            ConsumerGroupDescription consumerGroupDescription = describeConsumerGroupsResult.describedGroups().get(groupId).get();
            if (consumerGroupDescription.members().isEmpty()) {
                log.warn("No members found in consumer group '{}'.", groupId);
                throw new CommonCustomException(HttpStatus.NOT_FOUND.value(), "No members found in consumer group '" + groupId + "'.");
            }
            consumerGroupDescription.members().forEach(memberDescription -> {
                MemberInfo memberInfo = MemberInfo.builder()
                        .id(memberDescription.consumerId())
                        .clientId(memberDescription.clientId())
                        .host(memberDescription.host())
                        .assignment(memberDescription.assignment().topicPartitions().stream().map(topicPartition -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("topic", topicPartition.topic());
                            map.put("partition", String.valueOf(topicPartition.partition()));
                            return map;
                        }).toList())
                        .build();
                memberInfos.add(memberInfo);
            });
            log.info("Member information for group '{}' fetched successfully: {}", groupId, Utility.objectToJsonString(memberInfos));
            return memberInfos;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof GroupIdNotFoundException) {
                log.warn("⚠Consumer group '{}' not found. Please check the group ID and try again.", groupId);
                throw new CommonCustomException(HttpStatus.NOT_FOUND.value(), "Consumer group with id '" + groupId + "' not found. Please verify the group ID.");
            } else {
                log.error("Error while fetching member info for group '{}': {}", groupId, e.getMessage(), e);
                throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to fetch member info for group '" + groupId + "'. Please try again later.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Member info fetch for group '{}' was interrupted. Reason: {}", groupId, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Member info fetch was interrupted. Please try again.");
        } catch (Exception e) {
            log.error("Unexpected error while fetching member info for group '{}': {}", groupId, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while fetching member info. Please contact support.");
        }
    }

    /**
     * Fetches the offsets for a given consumer group.
     *
     * @param groupId the ID of the consumer group
     * @return a list of OffsetInfo objects representing the offsets of the specified consumer group
     * @throws CommonCustomException if the consumer group is not found or if there is an error while fetching offsets
     */
    @Override
    public List<OffsetInfo> getOffsets(String groupId) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            log.info("Fetching offsets for consumer group '{}'.", groupId);
            List<OffsetInfo> offsetInfos = new ArrayList<>();
            ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = adminClient.listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> offsets = listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get();
            if (offsets.isEmpty()) {
                log.warn("No offsets found in consumer group '{}'.", groupId);
                throw new CommonCustomException(HttpStatus.NOT_FOUND.value(), "No offsets found in consumer group '" + groupId + "'.");
            }
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(kafkaConsumerProps.getProps())) {
                offsets.forEach((topicPartition, offsetAndMetadata) -> {
                    OffsetInfo offsetInfo = OffsetInfo.builder()
                            .topic(topicPartition.topic())
                            .partition(topicPartition.partition())
                            .offset(offsetAndMetadata.offset())
                            .firstOffset(consumer.beginningOffsets(Collections.singletonList(topicPartition)).get(topicPartition))
                            .lastOffset(consumer.endOffsets(Collections.singletonList(topicPartition)).get(topicPartition))
                            .offsetLag(Math.max(consumer.endOffsets(Collections.singletonList(topicPartition)).get(topicPartition) - offsetAndMetadata.offset(), 0))
                            .metadata(offsetAndMetadata.metadata())
                            .build();
                    offsetInfos.add(offsetInfo);
                });
            } catch (Exception e) {
                log.error("Error while creating KafkaConsumer: {}", e.getMessage(), e);
            }
            log.info("Offsets for group '{}' fetched successfully: {}", groupId, Utility.objectToJsonString(offsetInfos));
            return offsetInfos;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof GroupIdNotFoundException) {
                log.warn("Consumer group with id {} not found.", groupId);
                throw new CommonCustomException(HttpStatus.NOT_FOUND.value(), "Consumer group with id " + groupId + " not found.");
            } else {
                log.error("Error while fetching offsets for groupId {}: {}", groupId, e.getMessage(), e);
                throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while fetching offsets for groupId " + groupId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error while fetching offsets for groupId {}: {}", groupId, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fetching offsets was interrupted.");
        } catch (Exception e) {
            log.error("Error while fetching offsets for groupId {}: {}", groupId, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while fetching offsets for groupId " + groupId);
        }
    }
}

