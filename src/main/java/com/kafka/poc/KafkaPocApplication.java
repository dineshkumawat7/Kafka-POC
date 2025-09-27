package com.kafka.poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Kafka Proof of Concept (POC) Spring Boot application.
 * <p>
 * This class bootstraps the Spring Boot application and enables component scanning, auto-configuration,
 * and other Spring Boot features via the {@link SpringBootApplication} annotation.
 * </p>
 *
 * <p>To start the application, run the {@code main} method. The application will initialize the Spring
 * context and start the embedded server.</p>
 *
 * <p>Example usage:
 * <pre>
 *     java -jar kafka-poc.jar
 * </pre>
 * </p>
 */
@SpringBootApplication
public class KafkaPocApplication {

    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(KafkaPocApplication.class, args);
    }

}
