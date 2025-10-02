package com.kafka.poc.service.impl;

import com.kafka.poc.exception.CommonCustomException;
import com.kafka.poc.model.BrokerInfo;
import com.kafka.poc.model.LogDirInfo;
import com.kafka.poc.service.BrokerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.requests.DescribeLogDirsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BrokerServiceImpl implements BrokerService {

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
}
