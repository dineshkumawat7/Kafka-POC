package com.kafka.poc.service.impl;

import com.kafka.poc.exception.CommonCustomException;
import com.kafka.poc.model.BrokerConfigs;
import com.kafka.poc.model.BrokerInfo;
import com.kafka.poc.model.BrokerLogs;
import com.kafka.poc.service.BrokerService;
import com.kafka.poc.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BrokerServiceImpl implements BrokerService {

    /**
     * KafkaAdmin for managing Kafka cluster operations.
     */
    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Override
    public List<BrokerInfo> getAllBrokers() {
        log.info("Initiating retrieval of Kafka broker information from the cluster.");
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult describeClusterResult = adminClient.describeCluster();
            Collection<Node> nodes = describeClusterResult.nodes().get();
            List<Integer> brokerIds = nodes.stream().map(Node::id).toList();
            Map<Integer, BrokerInfo> brokerMap = nodes.stream()
                    .collect(Collectors.toMap(
                            Node::id,
                            node -> BrokerInfo.builder()
                                    .id(node.id())
                                    .host(node.host())
                                    .port(node.port())
                                    .rack(node.rack())
                                    .build()
                    ));
            List<ConfigResource> brokerResources = brokerIds.stream()
                    .map(id -> new ConfigResource(ConfigResource.Type.BROKER, String.valueOf(id)))
                    .collect(Collectors.toList());
            DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(brokerResources);
            Map<ConfigResource, Config> configs = describeConfigsResult.all().get();
            for (Map.Entry<ConfigResource, Config> e : configs.entrySet()) {
                int id = Integer.parseInt(e.getKey().name());
                Map<String, String> configMap = new HashMap<>();
                for (ConfigEntry entry : e.getValue().entries()) {
                    configMap.put(entry.name(), entry.value());
                }
                BrokerInfo brokerInfo = brokerMap.get(id);
                if (brokerInfo != null) brokerInfo.setConfigs(configMap);
            }

            log.info("Kafka broker information retrieval successful. Total brokers found: {}.", brokerMap.size());
            return new ArrayList<>(brokerMap.values());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted while fetching broker info.", ie);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Request interrupted while fetching broker info.");
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching broker info.", e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while fetching kafka brokers info.");
        }
    }

    /**
     * Retrieves the configuration details for a specific Kafka broker.
     * <p>
     * This method fetches all configuration entries for the given broker ID and returns the details
     * as a BrokerConfig object. If multiple configs exist, only the first is returned. All exceptions
     * are handled gracefully with user-friendly log messages and error responses.
     * </p>
     *
     * @param brokerId the ID of the broker whose configuration is to be fetched
     * @return BrokerConfig containing configuration details, or null if not found
     * @throws CommonCustomException if an error occurs during retrieval
     */
    @Override
    public List<BrokerConfigs> getBrokerConfig(int brokerId) {
        List<BrokerConfigs> brokerConfigs = new ArrayList<>();
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            log.info("Starting configuration retrieval for Kafka broker with ID: {}...", brokerId);
            ConfigResource configResource = new ConfigResource(ConfigResource.Type.BROKER, String.valueOf(brokerId));
            DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(Collections.singletonList(configResource));
            Config config = describeConfigsResult.all().get(30, TimeUnit.SECONDS).get(configResource);
            config.entries().stream().map(configEntry -> BrokerConfigs.builder()
                            .name(configEntry.name())
                            .value(configEntry.value())
                            .description(configEntry.documentation())
                            .source(configEntry.source().name())
                            .type(String.valueOf(configEntry.type()))
                            .isSensitive(configEntry.isSensitive())
                            .isReadOnly(configEntry.isReadOnly())
                            .isDefault(configEntry.isDefault())
                            .synonyms(configEntry.synonyms())
                            .build())
                    .forEach(brokerConfigs::add);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Configuration retrieval for broker {} was interrupted. Reason: {}", brokerId, ie.getMessage(), ie);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Request interrupted while fetching broker configuration. Please try again.");
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching configuration for broker {}: {}", brokerId, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while fetching broker configuration. Please contact support.");
        }
        log.info("Configuration retrieval for Kafka broker with ID: {} completed successfully. {}", brokerId, Utility.objectToJsonString(brokerConfigs));
        return brokerConfigs;
    }

    @Override
    public List<BrokerLogs> getBrokerLogs(int brokerId) {
        try(AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())){
            log.info("Starting log retrieval for Kafka broker with ID: {}...", brokerId);
            List<BrokerLogs> brokerLogs = new ArrayList<>();
            DescribeLogDirsResult describeLogDirsResult = adminClient.describeLogDirs(Collections.singleton(brokerId));
            describeLogDirsResult.descriptions().get(brokerId);
            describeLogDirsResult.all().get(30, TimeUnit.SECONDS);
            log.info("Log retrieval for Kafka broker with ID: {} completed successfully. {}", brokerId, Utility.objectToJsonString(brokerLogs));
            return brokerLogs;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Log retrieval for broker {} was interrupted. Reason: {}", brokerId, ie.getMessage(), ie);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Request interrupted while fetching broker logs. Please try again.");
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching logs for broker {}: {}", brokerId, e.getMessage(), e);
            throw new CommonCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred while fetching broker logs. Please contact support.");
        }
    }
}
