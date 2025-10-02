package com.kafka.poc.service;

import com.kafka.poc.model.BrokerInfo;

import java.util.List;

public interface BrokerService {

    /**
     * Fetches and returns a list of all brokers in the Kafka cluster.
     *
     * @return List of BrokerInfo objects representing all brokers.
     */
    List<BrokerInfo> getAllBrokers();
}
