package com.vbforge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import java.util.Properties;
 
/**
 * Configuration class for Kafka Producers & Consumers.
 * Central configurations for all scenarios.
 * Works with Docker Kafka (KRaft mode, no ZooKeeper).
 *
 * Best Practices Implemented:
 * - Environment variable support with sensible local defaults
 * - Singleton ObjectMapper (thread-safe, expensive to create)
 * - Production-ready producer defaults (idempotent, compressed)
 * - Consumer variants: auto-commit, manual commit, batch
 * - Proper timeout configurations
 */


public class KafkaConfig {
 
    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);
 
    // ===== KAFKA CONNECTION =====
    // Reads from environment variable if set, falls back to localhost for local Docker dev.
    // Example: export KAFKA_BOOTSTRAP_SERVERS=broker1:9092,broker2:9092
    public static final String BOOTSTRAP_SERVERS = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
 
    // ===== TOPIC NAMES =====
    public static final String TOPIC_TEST_CONNECTIVITY = "test-topic";
    public static final String TOPIC_SIMPLE            = "simple-topic";
 
    // ===== CONSUMER GROUP IDs =====
    public static final String GROUP_ID_SIMPLE = "consumer-group-simple";
 
    // ===== TIMEOUT CONFIGURATIONS =====
    public static final int DEFAULT_POLL_TIMEOUT_MS       = 1000;
    public static final int GRACEFUL_SHUTDOWN_TIMEOUT_SEC = 10;
    public static final int PRODUCER_CLOSE_TIMEOUT_SEC    = 5;
 
    // ===== SINGLETON OBJECT MAPPER =====
    // ObjectMapper is thread-safe after configuration and very expensive to instantiate.
    // Always reuse a single instance — never create one per call.
    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();
 
    private static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Supports Java 8+ time types (java.time.*)
        return mapper;
    }
 
    /**
     * Returns the shared, pre-configured ObjectMapper instance.
     * Thread-safe. Use this everywhere instead of creating new instances.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
 
    // =========================================================================
    // PRODUCER CONFIGS
    // =========================================================================
 
    /**
     * Creates production-ready producer configuration.
     *
     * Key decisions:
     * - Idempotent: prevents duplicate messages on retry (requires acks=all + max retries)
     * - Snappy compression: good balance of speed vs size, no extra dependencies
     * - Linger 10ms: allows small batches to form, improves throughput slightly
     */
    public static Properties createProducerConfig() {
        Properties props = new Properties();
 
        // Connection
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
 
        // Serializers
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
 
        // Reliability — idempotent producer (prevents duplicates on retry)
        // These three must be set together: idempotence=true requires acks=all and max retries
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,     "true");
        props.put(ProducerConfig.ACKS_CONFIG,                   "all");
        props.put(ProducerConfig.RETRIES_CONFIG,                Integer.MAX_VALUE);
 
        // Performance tuning
        props.put(ProducerConfig.BATCH_SIZE_CONFIG,    16384);     // 16KB batch size
        props.put(ProducerConfig.LINGER_MS_CONFIG,     10);        // Wait up to 10ms to fill a batch
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);  // 32MB total buffer
 
        // Compression (reduces network traffic, snappy = fast + decent ratio)
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
 
        // Timeouts
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 2 min total delivery timeout
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,   30000); // 30s per request attempt
 
        return props;
    }
 
    // =========================================================================
    // CONSUMER CONFIGS
    // =========================================================================
 
    /**
     * Standard consumer config with auto-commit enabled.
     *
     * Suitable for: high-throughput, non-critical scenarios where losing
     * a few messages on crash is acceptable.
     *
     * NOT suitable for: financial, transactional, or exactly-once scenarios.
     *
     * @param groupId consumer group identifier
     */
    public static Properties createConsumerConfig(String groupId) {
        log.info("Creating auto-commit consumer config for group: {}", groupId);
 
        Properties props = new Properties();
 
        // Connection
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
 
        // Consumer group
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
 
        // Deserializers
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
 
        // Offset reset: where to start if no committed offset exists for this group
        // "earliest" = read from the beginning of the topic (good for dev/learning)
        // "latest"   = only read new messages arriving after consumer starts (good for prod)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
 
        // Auto-commit: Kafka commits offsets automatically every 5 seconds
        // Simple but risky — if the app crashes after poll() but before processing,
        // those messages are lost (offset already committed)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,        "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,   "5000");
 
        // Poll configuration
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,    "100");    // Max records per poll()
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000"); // 5 min max processing time between polls
 
        // Session & heartbeat
        // Rule: heartbeat.interval < session.timeout / 3
        // Here: 3000 < 45000/3=15000 ✅
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");  // Ping broker every 3s
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,    "45000"); // Declared dead after 45s of silence
 
        // Fetch configuration
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG,           "1");       // Return immediately if any data available
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG,         "500");     // Wait max 500ms if below min bytes
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "1048576"); // 1MB max per partition per fetch
 
        return props;
    }
 
    /**
     * Consumer config with manual offset commit (auto-commit disabled).
     *
     * Use for: exactly-once semantics, transactional processing, or any scenario
     * where you must guarantee a message was fully processed before marking it done.
     *
     * You are responsible for calling consumer.commitSync() or consumer.commitAsync()
     * after successfully processing each batch.
     *
     * @param groupId consumer group identifier
     */
    public static Properties createManualCommitConsumerConfig(String groupId) {
        log.info("Creating manual-commit consumer config for group: {}", groupId);
 
        Properties props = createConsumerConfig(groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        // AUTO_COMMIT_INTERVAL_MS is irrelevant when auto-commit is off, but harmless to leave
        return props;
    }
 
    /**
     * Consumer config optimized for batch processing.
     *
     * Increases minimum fetch size so Kafka waits to accumulate more data
     * before returning a poll — reduces round trips at the cost of latency.
     *
     * Use for: bulk DB inserts, file writers, aggregation pipelines.
     *
     * @param groupId        consumer group identifier
     * @param maxPollRecords how many records to return per poll() call
     */
    public static Properties createBatchConsumerConfig(String groupId, int maxPollRecords) {
        log.info("Creating batch consumer config for group: {}, maxPollRecords: {}", groupId, maxPollRecords);
 
        Properties props = createManualCommitConsumerConfig(groupId);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(maxPollRecords));
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG,  "32768"); // Wait for at least 32KB before returning
        return props;
    }
 
    // =========================================================================
    // UTILITIES
    // =========================================================================
 
    /**
     * Logs current configuration summary. Call at startup to verify environment.
     */
    public static void verifyConfiguration() {
        log.info("=== Kafka Configuration ===");
        log.info("Bootstrap Servers : {}", BOOTSTRAP_SERVERS);
        log.info("Source            : {}",
                System.getenv("KAFKA_BOOTSTRAP_SERVERS") != null ? "environment variable" : "default (localhost)");
        log.info("Topics            : {}, {}", TOPIC_TEST_CONNECTIVITY, TOPIC_SIMPLE);
        log.info("===========================");
    }
}