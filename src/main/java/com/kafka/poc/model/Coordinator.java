package com.kafka.poc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coordinator {
    private int id;
    private String idString;
    private String host;
    private int port;
    private String rack;
}
