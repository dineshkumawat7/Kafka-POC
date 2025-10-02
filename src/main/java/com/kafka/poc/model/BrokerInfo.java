package com.kafka.poc.model;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerInfo {
    private int id;
    private String host;
    private int port;
    private String rack;
    private Map<String, String> configs;
    private List<LogDirInfo> logDirs;
    private BrokerPartitionSummary partitionSummary;
    private Map<String, Object> jmxMetrics;
}