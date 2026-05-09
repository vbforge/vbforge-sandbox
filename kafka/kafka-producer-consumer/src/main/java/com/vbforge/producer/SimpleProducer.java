package com.vbforge.producer;

import com.vbforge.config.KafkaConfig;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SimpleProducer - Production-ready message producer
 * 
 * Best Practices Implemented:
 * - Configurable message count and delay
 * - Async sends with proper callbacks
 * - Metrics tracking (success/failure counts, send times)
 * - Idempotent producer configuration (prevents duplicates)
 * - Batch processing optimization
 * - Proper resource cleanup
 * 
 * Use Case: Simple message queue with guaranteed order and delivery
 */
public class SimpleProducer {

    private static final Logger logger = LoggerFactory.getLogger(SimpleProducer.class);
    
    // Configuration constants
    private static final int DEFAULT_MESSAGE_COUNT = 10;
    private static final int DEFAULT_DELAY_MS = 500;
    private static final int SEND_TIMEOUT_SEC = 30;
    
    // Metrics tracking
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failureCount = new AtomicInteger(0);
    private static long startTime;

    public static void main(String[] args) {
        // Allow overriding defaults via command line
        int messageCount = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_MESSAGE_COUNT;
        int delayMs = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_DELAY_MS;
        
        startSimpleProducer(messageCount, delayMs);
    }

    private static void startSimpleProducer(int messageCount, int delayMs) {
        logger.info("🚀 Starting SimpleProducer (Docker Kafka)");
        logger.info("📋 Configuration: {} messages, {}ms delay", messageCount, delayMs);
        KafkaConfig.verifyConfiguration();
        
        startTime = System.currentTimeMillis();
        
        // Create producer with production configuration
        Properties props = KafkaConfig.createProducerConfig();
        
        // Try-with-resources ensures proper cleanup
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            
            logger.info("✅ Producer created successfully");
            logger.info("📤 Sending {} messages to topic: {}", messageCount, KafkaConfig.TOPIC_SIMPLE);
            
            // Send messages in batch
            for (int i = 1; i <= messageCount; i++) {
                sendMessage(producer, i, delayMs);
            }
            
            // Ensure all messages are sent before closing
            logger.info("⏳ Flushing remaining messages...");
            producer.flush();
            
            // Print final statistics
            printFinalStatistics(messageCount);
            
        } catch (Exception e) {
            logger.error("❌ SimpleProducer failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send a single message with async callback
     */
    private static void sendMessage(KafkaProducer<String, String> producer, int sequence, int delayMs) {
        String message = String.format("Message #%d from Docker Kafka - Timestamp: %d", 
            sequence, System.currentTimeMillis());
        
        // Create record with key for partition affinity (optional)
        // Using null key for round-robin distribution across partitions
        ProducerRecord<String, String> record = new ProducerRecord<>(
            KafkaConfig.TOPIC_SIMPLE,
            null,  // null key = round-robin distribution
            message
        );
        
        long sendStartTime = System.currentTimeMillis();
        
        // Send asynchronously with callback
        producer.send(record, new SendCallback(message, sequence, sendStartTime));
        
        // Respect configured delay between sends (but don't block unnecessarily)
        if (delayMs > 0 && sequence < 10) {  // Don't delay after last message
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Producer interrupted during delay");
            }
        }
    }
    
    /**
     * Send message synchronously (alternative approach for critical messages)
     * Use when you need immediate confirmation
     */
    private static void sendMessageSync(KafkaProducer<String, String> producer, int sequence) 
            throws InterruptedException, ExecutionException, TimeoutException {
        
        String message = String.format("Sync Message #%d", sequence);
        ProducerRecord<String, String> record = new ProducerRecord<>(KafkaConfig.TOPIC_SIMPLE, message);
        
        RecordMetadata metadata = producer.send(record).get(SEND_TIMEOUT_SEC, TimeUnit.SECONDS);
        
        logger.info("✅ Sync message sent successfully");
        logger.info("   Message: {}", message);
        logger.info("   Partition: {}", metadata.partition());
        logger.info("   Offset: {}", metadata.offset());
    }
    
    /**
     * Print final statistics and performance metrics
     */
    private static void printFinalStatistics(int expectedCount) {
        long totalTime = System.currentTimeMillis() - startTime;
        int totalSuccess = successCount.get();
        int totalFailure = failureCount.get();
        
        logger.info("═══════════════════════════════════════════");
        logger.info("📊 FINAL STATISTICS:");
        logger.info("   Expected messages: {}", expectedCount);
        logger.info("   Successfully sent: {}", totalSuccess);
        logger.info("   Failed: {}", totalFailure);
        logger.info("   Total time: {} ms", totalTime);
        logger.info("   Throughput: {:.2f} msgs/sec", (totalSuccess * 1000.0 / totalTime));
        
        if (totalSuccess == expectedCount) {
            logger.info("✅ ALL MESSAGES SENT SUCCESSFULLY!");
        } else {
            logger.warn("⚠️ Some messages failed to send. Check Kafka connectivity.");
        }
        logger.info("═══════════════════════════════════════════");
    }
    
    /**
     * Custom callback implementation for handling send results
     */
    private static class SendCallback implements Callback {
        private final String message;
        private final int sequence;
        private final long sendStartTime;
        
        public SendCallback(String message, int sequence, long sendStartTime) {
            this.message = message;
            this.sequence = sequence;
            this.sendStartTime = sendStartTime;
        }
        
        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            long sendTime = System.currentTimeMillis() - sendStartTime;
            
            if (exception == null) {
                // Success case
                successCount.incrementAndGet();
                logger.info("✅ Message #{} sent successfully ({}ms)", sequence, sendTime);
                logger.info("   Value: {}", message);
                logger.info("   Topic: {}", metadata.topic());
                logger.info("   Partition: {}", metadata.partition());
                logger.info("   Offset: {}", metadata.offset());
                logger.info("   Timestamp: {}", metadata.timestamp());
            } else {
                // Failure case
                failureCount.incrementAndGet();
                logger.error("❌ Message #{} failed to send ({}ms)", sequence, sendTime);
                logger.error("   Message: {}", message);
                logger.error("   Error: {}", exception.getMessage(), exception);
                
                // Implement retry logic if needed
                // Additional handling could be added here (e.g., send to DLQ)
            }
        }
    }
}