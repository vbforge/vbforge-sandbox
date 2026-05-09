package com.vbforge.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestKafkaConnection {

    public static final String TEST_KEY = "test-key";
    public static final String TEST_VALUE = "Hello from Docker Kafka!";
    private static final Logger log = LoggerFactory.getLogger(TestKafkaConnection.class);

    public static void main(String[] args) {
        log.info("Test Kafka Configuration Connectivity Started...");

        KafkaConfig.verifyConfiguration();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(KafkaConfig.createProducerConfig())) {

            producer.send(new ProducerRecord<>(KafkaConfig.TOPIC_TEST_CONNECTIVITY, TEST_KEY, TEST_VALUE));
            producer.flush();
            log.info("✅ Successfully connected to Docker Kafka!");

        } catch (Exception e) {
            log.error("❌ Failed to connect: {}", e.getMessage());
            e.printStackTrace();
        }

        log.info("Test Kafka Configuration Connectivity Completed!");
    }

}
