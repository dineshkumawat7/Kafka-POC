package com.kafka.poc.service;

import com.kafka.poc.model.BrokerConfigs;
import com.kafka.poc.model.BrokerInfo;
import com.kafka.poc.model.BrokerLogs;

import java.util.List;

public interface BrokerService {

    /**
     * Fetches and returns a list of all brokers in the Kafka cluster.
     *
     * @return List of BrokerInfo objects representing all brokers.
     */
    List<BrokerInfo> getAllBrokers();

    /**
     * Fetches and returns the configuration details of a specific broker by its ID.
     *
     * @param brokerId The ID of the broker.
     * @return BrokerConfig object containing the configuration details of the specified broker.
     */
    List<BrokerConfigs> getBrokerConfig(int brokerId);

    List<BrokerLogs> getBrokerLogs(int brokerId);
}
