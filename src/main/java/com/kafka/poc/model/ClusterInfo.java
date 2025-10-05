package com.kafka.poc.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterInfo {
    private String clusterId;
    private BrokerInfo controller;
    private List<BrokerInfo> brokers;
    private List<TopicInfo> topics;
    private List<ConsumerGroupInfo> consumerGroups;
}
