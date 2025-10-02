package com.kafka.poc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberInfo {
    private String memberId;
    private String clientId;
    private String host;
    private String assignment;
}
