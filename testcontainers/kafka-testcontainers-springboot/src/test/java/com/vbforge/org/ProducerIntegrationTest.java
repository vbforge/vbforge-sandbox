package com.vbforge.org;

import com.vbforge.org.model.EventMsg;
import com.vbforge.org.model.ProducerResponse;
import com.vbforge.org.service.EventProducerService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// JUNIOR NOTE: ProducerIntegrationTest uses RAW KafkaConsumer instances (not the Spring
// @KafkaListener) to verify what landed on the topic. This is intentional:
//
//   - The @KafkaListener in EventConsumerService runs continuously in the background.
//     If we used it here, messages produced by this test class would also be counted
//     by ConsumerIntegrationTest's latch (bleed-through).
//
//   - Raw consumers with unique group IDs + explicit offset seeks isolate each test
//     completely: we only read exactly the messages THIS test produced.
//
// Because we use raw consumers here, this test class does NOT autowire
// EventConsumerService and does NOT call resetLatch(). It is fully decoupled.

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProducerIntegrationTest extends AbstractKafkaIntegrationTest{

    public static final String EVENTS_TOPIC = "events-topic";

    @Autowired
    private EventProducerService producerService;

    @Test
    @DisplayName("send batch: produced messages should appear on the topic with correct content")
    @Order(1)
    public void sendBatch_shouldProduceMessagesToKafka(){

        // GIVEN
        int count = 3;
        String type = "ORDER_PLACED";
        long offsetBefore = getEndOffset(EVENTS_TOPIC);

        // WHEN
        ProducerResponse response = producerService.sendBatch(count, type);

        // THEN (case 1) — ProducerResponse is correct
        assertThat(response.getMessageSent()).isEqualTo(count);
        assertThat(response.getMessages()).hasSize(count);
        assertThat(response.getSentAt()).isNotNull();
        response.getMessages().forEach(message -> {
            assertThat(message.getMessageId()).isNotBlank();
            assertThat(message.getOffset()).isGreaterThanOrEqualTo(0);
        });

        // THEN (case 2) — records actually landed on the topic
        List<ConsumerRecord<String, EventMsg>> consumed = consumeFromOffset(
                EVENTS_TOPIC, count, "producer-test-group-" + UUID.randomUUID(), offsetBefore);
        assertThat(consumed).hasSize(count);
        consumed.forEach(record -> {
            assertThat(record.value().getType()).isEqualTo(type);
            assertThat(record.value().getPayload()).isNotBlank();
            assertThat(record.value().getCreatedAt()).isNotNull();
        });

    }

    @Test
    @Order(2)
    @DisplayName("send single: a single EventMsg should appear on the topic exactly once")
    public void sendSingle_shouldProduceOneRecord() {
        // GIVEN
        String uniqueId = UUID.randomUUID().toString();
        EventMsg eventMsg = EventMsg.builder()
                .id(uniqueId)
                .type("USER_REGISTERED")
                .payload("test-payload-" + uniqueId)
                .sequenceNumber(1)
                .createdAt(LocalDateTime.now())
                .build();

        // JUNIOR NOTE: We snapshot the end offset BEFORE sending so the raw consumer
        // seeks past any messages left by previous tests. This is the correct isolation
        // pattern when multiple tests share one topic and auto.offset.reset=earliest
        // would otherwise replay all prior messages.
        long endOffsetBefore = getEndOffset(EVENTS_TOPIC);
        producerService.sendSingle(eventMsg);

        // THEN
        List<ConsumerRecord<String, EventMsg>> consumed = consumeFromOffset(
                EVENTS_TOPIC, 1, "single-test-group-" + UUID.randomUUID(), endOffsetBefore);

        assertThat(consumed).hasSize(1);
        EventMsg received = consumed.getFirst().value();
        assertThat(received.getId()).isEqualTo(uniqueId);
        assertThat(received.getType()).isEqualTo("USER_REGISTERED");
        assertThat(received.getPayload()).isEqualTo("test-payload-" + uniqueId);
    }



    //helpers methods

    /**
     * Snapshot the current end offset of partition 0 before sending.
     * Used to seek the raw consumer past any messages from prior tests.
     *
     * JUNIOR NOTE: We use a unique group ID so this probe consumer doesn't interfere
     * with any group offset commits elsewhere. StringDeserializer is fine here because
     * we only care about the offset position, not the message value.
     */
    private long getEndOffset(String topic) {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "offset-probe-" + UUID.randomUUID(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            var tp = new org.apache.kafka.common.TopicPartition(topic, 0);
            consumer.assign(List.of(tp));
            consumer.seekToEnd(List.of(tp));
            return consumer.position(tp);
        }
    }

    /**
     * Reads exactly {@code expectedCount} records starting from {@code startOffset}.
     * Isolates each test to only the messages it produced, regardless of what prior
     * tests left on the topic.
     *
     * JUNIOR NOTE: We use assign() + seek() instead of subscribe() here. subscribe()
     * triggers group rebalancing and the first poll can be slow (up to session.timeout.ms).
     * assign() is synchronous — the consumer is immediately ready to fetch from the
     * exact offset we specify. This is faster and more predictable in tests.
     */
    private List<ConsumerRecord<String, EventMsg>> consumeFromOffset(
            String topic, int expectedCount, String groupId, long startOffset) {

        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class,
                JacksonJsonDeserializer.TRUSTED_PACKAGES, "*",
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, EventMsg.class.getName()
        );

        List<ConsumerRecord<String, EventMsg>> result = new ArrayList<>();
        try (KafkaConsumer<String, EventMsg> consumer = new KafkaConsumer<>(props)) {
            var tp = new org.apache.kafka.common.TopicPartition(topic, 0);
            consumer.assign(List.of(tp));
            consumer.seek(tp, startOffset);   // skip past all prior messages
            long deadline = System.currentTimeMillis() + 15_000;
            while (result.size() < expectedCount && System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, EventMsg> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(result::add);
            }
        }
        return result;
    }

    /**
     * Alternative helper: reads up to {@code expectedCount} records from the BEGINNING
     * of the topic using a unique consumer group.
     *
     * WHEN TO USE THIS vs consumeFromOffset():
     *   - consumeFromOffset() → use when other tests have also written to the same topic
     *     and you only want to verify YOUR messages. This is the default.
     *   - consumeFromTopic() → use when the topic is EMPTY at test start and you want
     *     to read everything from the beginning, OR when you want to assert on the total
     *     number of messages ever produced (e.g. a duplicate-detection test).
     *
     * Example scenario for consumeFromTopic():
     *   Suppose you clear the topic before a test (or this is the very first test that
     *   runs on a freshly-created topic). You then produce N messages and want to assert
     *   that exactly N landed — with no offset arithmetic needed:
     *
     *   <pre>
     *   producerService.sendBatch(3, "ORDER_PLACED");
     *   List<ConsumerRecord<String, EventMsg>> all =
     *       consumeFromTopic(EVENTS_TOPIC, 3, "full-scan-" + UUID.randomUUID());
     *   assertThat(all).hasSize(3);
     *   </pre>
     *
     * JUNIOR NOTE: Because auto.offset.reset=earliest and each call uses a new unique
     * group ID, this consumer always starts from offset 0 and replays the full topic
     * history. If prior tests left messages on the topic, those are included. Use only
     * when that is intentional.
     */
    private List<ConsumerRecord<String, EventMsg>> consumeFromTopic(
            String topic, int expectedCount, String groupId) {

        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class,
                JacksonJsonDeserializer.TRUSTED_PACKAGES, "*",
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, EventMsg.class.getName()
        );

        List<ConsumerRecord<String, EventMsg>> result = new ArrayList<>();
        try (KafkaConsumer<String, EventMsg> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(topic));
            // JUNIOR NOTE: We poll in a loop because poll() may not return all records
            // in one call — especially on the first call when metadata is being fetched.
            // We stop when we've collected enough OR we've exceeded the deadline.
            long deadline = System.currentTimeMillis() + 15_000;
            while (result.size() < expectedCount && System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, EventMsg> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(result::add);
            }
        }
        return result;
    }



}
