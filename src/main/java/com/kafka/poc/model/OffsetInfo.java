package com.kafka.poc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffsetInfo {
    private String topic;
    private int partition;
    private long offset;
    private long firstOffset;
    private long lastOffset;
    private long offsetLag;
    private String metadata;
}
