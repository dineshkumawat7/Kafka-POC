package com.kafka.poc.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration class for setting up KafkaAdmin bean.
 * <p>
 * This configuration provides the necessary KafkaAdmin instance for Kafka topic and cluster administration
 * using the provided bootstrap server(s). The KafkaAdmin bean is used by the application to perform
 * administrative operations such as creating, deleting, and describing topics.
 * </p>
 *
 * <p><b>Note:</b> Update the bootstrap server address as per your Kafka cluster configuration.</p>
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;

    /**
     * Creates and configures a KafkaAdmin bean for Kafka administration operations.
     *
     * @return a configured KafkaAdmin instance
     */
    @Bean
    public KafkaAdmin kafkaAdmin(){
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        return new KafkaAdmin(configs);
    }
}
