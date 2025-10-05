package com.kafka.poc.model;

import lombok.*;
import org.apache.kafka.common.acl.AclOperation;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerGroupInfo {
    private String id;
    private String state;
    private boolean isSimpleConsumerGroup;
    private String type;
    private String partitionAssignor;
    private Coordinator coordinator;
    private List<MemberInfo> members;
    private List<OffsetInfo> offsets;
    private List<String> topics;
    private List<String> activeTopics;
    private Set<AclOperation> authorizedOperations;
}
