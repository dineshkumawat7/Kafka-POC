package com.kafka.poc.model;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberInfo {
    private String id;
    private String clientId;
    private String host;
    private List<Map<String, Object>> assignment;
}
