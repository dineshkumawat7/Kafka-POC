package com.kafka.poc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerPartitionSummary {
    private int leaderPartitionCount;
    private int followerReplicaCount;
    private int offlineReplicaCount;
    private int totalReplicas;
}
