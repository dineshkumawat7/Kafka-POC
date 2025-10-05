package com.kafka.poc.service;

import com.kafka.poc.model.ConsumerGroupInfo;
import com.kafka.poc.model.Coordinator;
import com.kafka.poc.model.MemberInfo;
import com.kafka.poc.model.OffsetInfo;

import java.util.List;

/**
 * Service interface for managing Kafka consumer groups.
 */
public interface ConsumerGroupService {

    /**
     * Retrieves information about all consumer groups in the Kafka cluster.
     *
     * @return a list of ConsumerGroupInfo objects representing all consumer groups
     */
    List<ConsumerGroupInfo> getAllConsumerGroups();

    /**
     * Retrieves the coordinator information for a specific consumer group.
     *
     * @param groupId the ID of the consumer group
     * @return a Coordinator object representing the coordinator of the specified consumer group
     */
    Coordinator getCoordinator(String groupId);

    /**
     * Retrieves member information for a specific consumer group.
     *
     * @param groupId the ID of the consumer group
     * @return a list of MemberInfo objects representing the members of the specified consumer group
     */
    List<MemberInfo> getMembers(String groupId);

    /**
     * Retrieves offset information for a specific consumer group.
     *
     * @param groupId the ID of the consumer group
     * @return a list of OffsetInfo objects representing the offsets of the specified consumer group
     */
    List<OffsetInfo> getOffsets(String groupId);
}
