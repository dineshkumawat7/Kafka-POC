package com.kafka.poc.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

/**
 * Health indicator for Kafka cluster connectivity.
 * Provides user-friendly and informative health status messages.
 */
@Component("kafka")
public class KafkaHealthIndicator implements HealthIndicator {

    @Autowired
    private KafkaAdmin kafkaAdmin;

    /**
     * Checks the health of the Kafka cluster connection.
     *
     * @return Health status with user-friendly details and suggestions.
     */
    @Override
    public Health health() {
        Health.Builder health = Health.up();
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult describeClusterResult = adminClient.describeCluster();
            health.withDetail("message", "Successfully connected to the Kafka cluster. All systems are operational.");
            health.withDetail("clusterId", describeClusterResult.clusterId().get());
            health.withDetail("controller", describeClusterResult.controller().get());
            health.withDetail("nodes", describeClusterResult.nodes().get());
        } catch (Exception e) {
            health.down()
                    .withDetail("error", "Unable to connect to Kafka cluster: " + e.getMessage())
                    .withDetail("suggestion", "Please verify your Kafka server address, network connectivity, and cluster status.");
        }
        return health.build();
    }
}
