package com.kafka.poc.model;

import lombok.*;
import org.apache.kafka.clients.admin.ConfigEntry;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerConfigs {
    private String name;
    private String value;
    private String description;
    private String source;
    private String type;
    private boolean isSensitive;
    private boolean isReadOnly;
    private boolean isDefault;
    private List<ConfigEntry.ConfigSynonym> synonyms;
}
