package com.vbforge.org;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

// @Testcontainers on the abstract base class means JUnit 5 manages the
// container lifecycle for every subclass automatically. The @Container field is static,
// so ONE Kafka container is started for the entire test suite and reused across all three
// test classes (ProducerIntegrationTest, ConsumerIntegrationTest, E2EIntegrationTest).
// This is intentional — spinning up a fresh container per class would triple test time.
//
// KEY RULE: because all test classes share one Spring ApplicationContext (same bootstrap-
// servers value → Spring caches the context), they also share the single EventConsumerService
// bean and its @KafkaListener. Isolation between tests is handled at the latch/collection
// level (see resetLatch()), NOT by restarting Kafka or Spring.

/**
 * need to consider the version of running all tests at ones:
 * Isolate either by using different topics per test class OR by making the EventConsumerService aware of which test class is currently running
 * and maintaining separate state for each.
 *
 * Now it is work if one test run independently.
 * */

@Testcontainers
public abstract class AbstractKafkaIntegrationTest {

    @Container
    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:8.0.0")
    );

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }


}
