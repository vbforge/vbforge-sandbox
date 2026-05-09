package com.vbforge.consumer;

import com.vbforge.config.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SimpleConsumer - Production-ready message consumer
 * 
 * Best Practices Implemented:
 * - Graceful shutdown with shutdown hook
 * - Proper resource cleanup
 * - Metrics tracking (message count, processing time)
 * - Configurable poll timeout
 * - Thread-safe shutdown flag
 * - Detailed logging with performance metrics
 * 
 * Use Case: Simple message queue with guaranteed order
 */
public class SimpleConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);
    
    // Thread-safe flag for shutdown coordination
    private static final AtomicBoolean running = new AtomicBoolean(true);
    
    // Metrics tracking
    private static final AtomicLong totalMessagesReceived = new AtomicLong(0);
    private static long startTime;

    public static void main(String[] args) {
        startSimpleConsumer();
    }

    public static void startSimpleConsumer() {
        logger.info("🚀 Starting SimpleConsumer (Docker Kafka)");
        KafkaConfig.verifyConfiguration();
        
        // Create consumer with standard configuration
        Properties props = KafkaConfig.createConsumerConfig(KafkaConfig.GROUP_ID_SIMPLE);
        
        // Register shutdown hook for graceful termination
        registerShutdownHook();
        
        startTime = System.currentTimeMillis();
        
        // Try-with-resources ensures proper resource cleanup
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            
            // Subscribe to topic
            consumer.subscribe(Collections.singletonList(KafkaConfig.TOPIC_SIMPLE));
            
            logger.info("✅ Subscribed to topic: {}", KafkaConfig.TOPIC_SIMPLE);
            logger.info("⏳ Waiting for messages... (Press Ctrl+C to stop)");
            logger.info("📊 Polling timeout: {}ms", KafkaConfig.DEFAULT_POLL_TIMEOUT_MS);
            
            // Main consumption loop
            while (running.get()) {
                try {
                    // Poll for messages with configured timeout
                    ConsumerRecords<String, String> records = consumer.poll(
                        Duration.ofMillis(KafkaConfig.DEFAULT_POLL_TIMEOUT_MS)
                    );
                    
                    if (records.isEmpty()) {
                        logger.debug("No messages received in this poll cycle");
                        continue;
                    }
                    
                    // Process batch of records
                    processRecords(records);
                    
                } catch (WakeupException e) {
                    // Ignore wakeup exception - triggered by shutdown
                    logger.info("Wakeup signal received, initiating shutdown...");
                    break;
                } catch (Exception e) {
                    logger.error("Error processing messages: {}", e.getMessage(), e);
                    // Continue processing - don't crash on transient errors
                }
            }
            
            // Log final statistics
            printFinalStatistics();
            
        } catch (Exception e) {
            logger.error("❌ SimpleConsumer startup failed: {}", e.getMessage(), e);
        }
        
        logger.info("🏁 SimpleConsumer finished!");
    }
    
    /**
     * Process batch of consumer records with metrics tracking
     */
    private static void processRecords(ConsumerRecords<String, String> records) {
        long batchStartTime = System.currentTimeMillis();
        int batchSize = records.count();
        
        logger.info("📦 Received batch of {} messages", batchSize);
        
        for (ConsumerRecord<String, String> record : records) {
            processSingleRecord(record);
        }
        
        long batchProcessingTime = System.currentTimeMillis() - batchStartTime;
        logger.info("✅ Batch processed in {}ms (avg {}/ms per msg)", batchProcessingTime,
                String.format("%.2f", (double) batchProcessingTime / batchSize));
    }
    
    /**
     * Process single consumer record with detailed logging
     */
    private static void processSingleRecord(ConsumerRecord<String, String> record) {
        long messageCount = totalMessagesReceived.incrementAndGet();
        
        logger.info("┌─────────────────────────────────────────────");
        logger.info("│ 📨 Message #{}", messageCount);
        logger.info("│    Value: {}", record.value());
        logger.info("│    Partition: {}", record.partition());
        logger.info("│    Offset: {}", record.offset());
        logger.info("│    Timestamp: {}", record.timestamp());
        logger.info("│    Key: {}", record.key() != null ? record.key() : "null");
        logger.info("└─────────────────────────────────────────────");
    }
    
    /**
     * Register shutdown hook for graceful termination
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🛑 Shutdown signal received");
            running.set(false);
            
            // Give consumer time to complete current poll
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            logger.info("Graceful shutdown completed");
        }, "consumer-shutdown-hook"));
    }
    
    /**
     * Print final statistics before exit
     */
    private static void printFinalStatistics() {
        long runtime = System.currentTimeMillis() - startTime;
        long messagesProcessed = totalMessagesReceived.get();
        double avgThroughput = messagesProcessed / (runtime / 1000.0);
        
        logger.info("═══════════════════════════════════════════");
        logger.info("📊 FINAL STATISTICS:");
        logger.info("   Messages processed: {}", messagesProcessed);
        logger.info("   Total runtime: {} ms", runtime);
        logger.info("   Average throughput: {:.2f} msgs/sec", avgThroughput);
        logger.info("═══════════════════════════════════════════");
    }
}