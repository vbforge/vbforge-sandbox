package com.vbforge.org;

import com.vbforge.org.model.EventMsg;
import com.vbforge.org.service.EventConsumerService;
import com.vbforge.org.service.EventProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

// JUNIOR NOTE: @TestMethodOrder(MethodOrderer.OrderAnnotation.class) makes test execution
// order explicit and deterministic. This matters because all tests share one Spring context
// and one EventConsumerService singleton. Without a fixed order, JUnit can interleave tests
// from different classes unpredictably, causing latch counts to mismatch.
// Each @Test here must start with consumerService.resetLatch(N) to clear messages left by
// whichever test ran before it.

@SpringBootTest
public class ConsumerIntegrationTest extends AbstractKafkaIntegrationTest{

    @Autowired
    private EventProducerService producerService;

    @Autowired
    private EventConsumerService consumerService;

    // JUNIOR NOTE: resetLatch(0) in @BeforeEach is a safety guard only — it prevents a
    // newly-created CountDownLatch(0) from blocking if a test forgot to call resetLatch().
    // The real reset with the correct count happens at the start of each individual test.
    @BeforeEach
    public void setup() {
        consumerService.resetLatch(0);
    }

    @Test
    @Order(1)
    @DisplayName("consumer should receive a single produced event within timeout")
    public void consumer_shouldReceiveSingleEvent() throws InterruptedException, ExecutionException, TimeoutException {
        // GIVEN
        consumerService.resetLatch(1);
        EventMsg event = EventMsg.builder()
                .id(UUID.randomUUID().toString())
                .type("ORDER_PLACED")
                .payload("test order payload")
                .sequenceNumber(1)
                .createdAt(LocalDateTime.now())
                .build();

        // WHEN
        producerService.sendSingle(event);
        boolean completed = consumerService.getLatch().await(15, TimeUnit.SECONDS);

        // THEN
        assertThat(completed).as("Latch should count down to 0 within 15s").isTrue();
        List<EventMsg> received = consumerService.getReceivedEvents();
        assertThat(received).hasSize(1);
        assertThat(received.getFirst().getId()).isEqualTo(event.getId());
        assertThat(received.getFirst().getType()).isEqualTo("ORDER_PLACED");
        assertThat(received.getFirst().getPayload()).isEqualTo("test order payload");
    }

    @Test
    @Order(2)
    @DisplayName("consumer should receive all events from a batch in order")
    void consumer_shouldReceiveAllBatchEvents() throws Exception {
        // GIVEN
        int count = 5;
        consumerService.resetLatch(count);

        // WHEN
        producerService.sendBatch(count, "USER_REGISTERED");
        boolean completed = consumerService.getLatch().await(15, TimeUnit.SECONDS);

        // THEN
        assertThat(completed).as("All 5 events should arrive within 15s").isTrue();

        List<EventMsg> received = consumerService.getReceivedEvents();
        assertThat(received).hasSize(count);
        received.forEach(e -> assertThat(e.getType()).isEqualTo("USER_REGISTERED"));

        // JUNIOR NOTE: Sequence numbers are asserted after sorting because Kafka only
        // guarantees order within a single partition. With one partition these will
        // always be ordered, but sorting makes the test partition-count-agnostic.
        List<Integer> sequences = received.stream()
                .map(EventMsg::getSequenceNumber)
                .sorted()
                .toList();
        assertThat(sequences).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @Order(3)
    @DisplayName("consumer should handle multiple event types in the same batch")
    void consumer_shouldHandleMixedEventTypes() throws Exception {
        // GIVEN
        consumerService.resetLatch(2);

        EventMsg e1 = EventMsg.builder()
                .id(UUID.randomUUID().toString()).type("ORDER_PLACED")
                .payload("order-001").sequenceNumber(1).createdAt(LocalDateTime.now()).build();
        EventMsg e2 = EventMsg.builder()
                .id(UUID.randomUUID().toString()).type("PAYMENT_PROCESSED")
                .payload("payment-001").sequenceNumber(2).createdAt(LocalDateTime.now()).build();

        // WHEN
        producerService.sendSingle(e1);
        producerService.sendSingle(e2);
        boolean completed = consumerService.getLatch().await(15, TimeUnit.SECONDS);

        // THEN
        assertThat(completed).isTrue();
        List<EventMsg> received = consumerService.getReceivedEvents();
        assertThat(received).hasSize(2);

        List<String> types = received.stream().map(EventMsg::getType).toList();
        assertThat(types).containsExactlyInAnyOrder("ORDER_PLACED", "PAYMENT_PROCESSED");
    }

    @Test
    @Order(4)
    @DisplayName("latch timeout — consumer that never receives message should fail fast, not hang")
    void consumer_latchTimeout_shouldFailFastNotHang() throws Exception {
        // JUNIOR NOTE: This test intentionally demonstrates the timeout behavior.
        // We set a latch for 1 message but don't produce anything.
        // The test should complete QUICKLY (< 5s wall-clock) with completed=false,
        // proving the test suite doesn't hang when Kafka consumption doesn't happen.
        consumerService.resetLatch(1);

        // WHEN — no message sent
        boolean completed = consumerService.getLatch().await(3, TimeUnit.SECONDS);

        // THEN
        assertThat(completed).as("Should NOT complete — no message was sent").isFalse();
    }


}
