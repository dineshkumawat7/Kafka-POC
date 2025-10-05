package com.kafka.poc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerLogs {
    private int brokerId;
    private String path;
    private String topic;
    private int partition;
    private long size;
    private long offsetLag;
    private boolean future;
}