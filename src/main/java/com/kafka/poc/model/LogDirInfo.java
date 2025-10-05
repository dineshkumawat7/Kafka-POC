package com.kafka.poc.model;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogDirInfo {
    private String path;
    private long totalSizeBytes;
    private Map<String, List<Integer>> topicPartitions;
}
