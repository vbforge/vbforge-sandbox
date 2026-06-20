package com.vbforge.org;

import com.vbforge.org.model.EventMsg;
import com.vbforge.org.service.EventConsumerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// JUNIOR NOTE: @TestMethodOrder ensures the order is deterministic when tests run together.
// Because the shared EventConsumerService latch can be decremented by any @KafkaListener
// firing in the background, every test MUST call consumerService.resetLatch(N) with the
// exact count it expects before it produces messages.
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class E2EIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EventConsumerService consumerService;

    @Value("${kafka.topics.events}")
    private String eventsTopic;

    @BeforeEach
    void setUp() {
        consumerService.resetLatch(0);
    }

    @Test
    @Order(1)
    @DisplayName("E2E: message produced via KafkaTemplate is consumed with identical content")
//    @Timeout(60)  // 60 seconds timeout
    void e2e_messageContentIntegrity() throws Exception {
        // GIVEN
        consumerService.resetLatch(1);
        String id = UUID.randomUUID().toString();
        String payload = "e2e-payload-" + id;

        EventMsg event = EventMsg.builder()
                .id(id)
                .type("INVENTORY_UPDATED")
                .payload(payload)
                .sequenceNumber(42)
                .createdAt(LocalDateTime.of(2025, 6, 1, 12, 0, 0))
                .build();

        // WHEN — send directly via KafkaTemplate (bypasses service layer)
        kafkaTemplate.send(eventsTopic, id, event).get();
        boolean received = consumerService.getLatch().await(15, TimeUnit.SECONDS);

        // THEN
        assertThat(received).isTrue();
        EventMsg consumed = consumerService.getReceivedEvents().get(0);

        assertThat(consumed.getId()).isEqualTo(id);
        assertThat(consumed.getType()).isEqualTo("INVENTORY_UPDATED");
        assertThat(consumed.getPayload()).isEqualTo(payload);
        assertThat(consumed.getSequenceNumber()).isEqualTo(42);
        // JUNIOR NOTE: LocalDateTime survives Jackson serialization because AppConfig
        // registers JavaTimeModule and disables WRITE_DATES_AS_TIMESTAMPS.
        assertThat(consumed.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 6, 1, 12, 0, 0));
    }

    @Test
    @Order(2)
    @DisplayName("E2E: 10 messages sent in rapid succession are all received")
//    @Timeout(60)  // 60 seconds timeout
    void e2e_rapidFireMessages_allReceived() throws Exception {
        // GIVEN
        int count = 10;
        consumerService.resetLatch(count);

        // WHEN — fire all 10 without waiting for acks (async)
        // JUNIOR NOTE: We call send() without .get() here — fire-and-forget style.
        // All 10 sends go to the broker buffer in rapid succession.
        // latch.await() below is what confirms all 10 were consumed.
        // This tests that the consumer keeps up under a small burst.
        for (int i = 0; i < count; i++) {
            EventMsg event = EventMsg.builder()
                    .id(UUID.randomUUID().toString())
                    .type("RAPID_EVENT")
                    .payload("rapid-" + i)
                    .sequenceNumber(i)
                    .createdAt(LocalDateTime.now())
                    .build();
            kafkaTemplate.send(eventsTopic, event.getId(), event);
        }

        boolean allReceived = consumerService.getLatch().await(20, TimeUnit.SECONDS);

        // THEN
        assertThat(allReceived).as("All 10 messages should be received within 20s").isTrue();
        assertThat(consumerService.getReceivedEvents()).hasSize(count);

        List<String> types = consumerService.getReceivedEvents().stream()
                .map(EventMsg::getType).distinct().toList();
        assertThat(types).containsOnly("RAPID_EVENT");
    }

    @Test
    @Order(3)
    @DisplayName("E2E: null payload field survives serialization round-trip")
//    @Timeout(60)  // 60 seconds timeout
    void e2e_nullFieldSurvivesRoundTrip() throws Exception {
        // JUNIOR NOTE: Jackson includes null fields as "field":null by default.
        // If the consumer's deserializer requires all fields to be non-null this fails.
        // Testing nulls explicitly prevents hard-to-debug production deserialization errors.
        consumerService.resetLatch(1);

        EventMsg event = EventMsg.builder()
                .id(UUID.randomUUID().toString())
                .type(null)           // intentionally null
                .payload("has-payload")
                .sequenceNumber(0)
                .createdAt(null)      // intentionally null
                .build();

        kafkaTemplate.send(eventsTopic, event.getId(), event).get();
        consumerService.getLatch().await(15, TimeUnit.SECONDS);

        EventMsg consumed = consumerService.getReceivedEvents().get(0);
        assertThat(consumed.getType()).isNull();
        assertThat(consumed.getCreatedAt()).isNull();
        assertThat(consumed.getPayload()).isEqualTo("has-payload");
    }
}
