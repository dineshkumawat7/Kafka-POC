package com.kafka.poc.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartitionInfo {
    private int partitionId;
    private BrokerInfo leader;
    private List<BrokerInfo> replicas;
    private List<BrokerInfo> inSyncReplicas;
}
