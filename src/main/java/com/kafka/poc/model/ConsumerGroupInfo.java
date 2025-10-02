package com.kafka.poc.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerGroupInfo {
    private String groupId;
    private String state; // Stable, Empty, Dead, etc.
    private List<MemberInfo> members;
}
