# kafka-testcontainers-springboot
Demonstrating how to write real **integration tests** for Kafka producers and consumers using **Testcontainers**


**Module:** `case-17` of `kafka-08-multi-module-concepts`  
**Stack:** Java 21 · Spring Boot 4.0.6 · Spring Kafka · Testcontainers 2.0.5 · ConfluentKafkaContainer · Maven

A self-contained learning project demonstrating how to write **real integration tests for Kafka producers and consumers** using Testcontainers — no mocked brokers, no embedded Kafka, no running Docker Compose required. One annotation starts a real Confluent Kafka broker inside a container for the duration of the test suite.

```  
* TODO:
* Ensure test isolation when executing the entire test suite.
*
* Currently, the tests pass when run individually, but shared state causes
* issues when all tests are executed together.
*
* Possible solutions:
* - Use dedicated Kafka topics for each test class.
* - Refactor EventConsumerService to maintain test-class-specific state
*   instead of relying on shared state.
```

---

## Project Structure

```
kafka-testcontainers-springboot/
├── src/
│   ├── main/
│   │   ├── java/com/vbforge/org/
│   │   │   ├── KafkaTestcontainersApp.java        # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java                 # Jackson ObjectMapper (JavaTimeModule)
│   │   │   │   └── KafkaConfig.java               # Manual producer/consumer/listener factory
│   │   │   ├── controller/
│   │   │   │   └── EventController.java           # REST endpoints (health, send, stats)
│   │   │   ├── model/
│   │   │   │   ├── EventMsg.java                  # Kafka message model
│   │   │   │   └── ProducerResponse.java          # Batch send response model
│   │   │   └── service/
│   │   │       ├── EventProducerService.java      # sendBatch() + sendSingle()
│   │   │       └── EventConsumerService.java      # @KafkaListener + test helpers
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/vbforge/org/
│           ├── AbstractKafkaIntegrationTest.java  # Shared Testcontainers setup
│           ├── ProducerIntegrationTest.java       # Raw KafkaConsumer verification
│           ├── ConsumerIntegrationTest.java       # @KafkaListener latch-based tests
│           └── E2EIntegrationTest.java            # Full produce→consume round-trips
└── pom.xml
```

---

## How It Works

### Testcontainers Setup

All three test classes extend `AbstractKafkaIntegrationTest`:

```java
@Testcontainers
public abstract class AbstractKafkaIntegrationTest {

    @Container
    static final ConfluentKafkaContainer kafka =
        new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:8.0.0"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
```

The `static` container field means **one Kafka broker is shared across all test classes**. Spring caches the `ApplicationContext` because `bootstrap-servers` resolves to the same value for all classes — so you also get one shared Spring context and one shared `EventConsumerService` instance.

### Test Isolation Strategy

Because the `@KafkaListener` never stops between tests, a message produced by Test A can arrive while Test B is running. Two patterns handle this:

| Pattern | Used in | How it isolates |
|---|---|---|
| **`resetLatch(N)` + `CountDownLatch`** | `ConsumerIntegrationTest`, `E2EIntegrationTest` | Atomically swaps the shared list and latch. Late messages from prior tests hit a discarded latch (count already 0 → no-op) and are not added to the new test's list. |
| **Raw consumer + unique group ID + `seek(offset)`** | `ProducerIntegrationTest` | Creates a temporary `KafkaConsumer` that reads only from the offset recorded before sending. Completely bypasses `EventConsumerService` — no bleed-through possible. |

---

## Running Tests

### Prerequisites

- Docker Desktop with **WSL2 backend** enabled
- Windows: the named pipe `\\.\pipe\docker_engine_linux` must be accessible

### Run all tests

```bash
mvn test
```

The `pom.xml` already configures `maven-surefire-plugin` with the correct `DOCKER_HOST` for WSL2:

```xml
<environmentVariables>
    <DOCKER_HOST>npipe:////./pipe/docker_engine_linux</DOCKER_HOST>
</environmentVariables>
```

### Run a single test class

```bash
mvn test -Dtest=ConsumerIntegrationTest
mvn test -Dtest=ProducerIntegrationTest
mvn test -Dtest=E2EIntegrationTest
```

### Run all tests together (correct order)

```bash
mvn test -Dtest="ProducerIntegrationTest,ConsumerIntegrationTest,E2EIntegrationTest"
```

---

## Test Classes

### `ProducerIntegrationTest`

Verifies that `EventProducerService` correctly writes messages to Kafka by consuming them back with a **raw `KafkaConsumer`**.

| Test | What it checks |
|---|---|
| `sendBatch_shouldProduceMessagesToKafka` | Batch of 3 messages lands on the topic with correct type/payload/timestamp |
| `sendSingle_shouldProduceOneRecord` | Single `EventMsg` appears exactly once with matching id/type/payload |

**Key pattern:** `getEndOffset()` snapshots the topic end before sending; `consumeFromOffset()` seeks to that position so only this test's messages are read.

### `ConsumerIntegrationTest`

Verifies that `EventConsumerService`'s `@KafkaListener` correctly receives and stores messages produced by `EventProducerService`.

| Test | What it checks |
|---|---|
| `consumer_shouldReceiveSingleEvent` | Single event received with correct id/type/payload |
| `consumer_shouldReceiveAllBatchEvents` | All 5 batch events received; sequence numbers 1–5 present |
| `consumer_shouldHandleMixedEventTypes` | Two different event types received in any order |
| `consumer_latchTimeout_shouldFailFastNotHang` | Latch times out in 3s when no message is sent (proves no infinite hang) |

### `E2EIntegrationTest`

Full end-to-end: sends via `KafkaTemplate` directly (bypassing the service layer) and asserts on what `EventConsumerService` received.

| Test | What it checks |
|---|---|
| `e2e_messageContentIntegrity` | All fields (id, type, payload, sequenceNumber, createdAt) survive the round-trip |
| `e2e_rapidFireMessages_allReceived` | 10 async sends all consumed within 20s |
| `e2e_nullFieldSurvivesRoundTrip` | Null `type` and `createdAt` fields serialize/deserialize correctly |

---

## REST API

The app also exposes HTTP endpoints for manual testing while running locally.

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/events/health` | Liveness check |
| `POST` | `/api/events/send/batch?count=5&type=ORDER_PLACED` | Send a batch of N messages |
| `POST` | `/api/events/send/single?type=ORDER_PLACED` | Send one message (body optional) |
| `GET` | `/api/events/stats` | Count of messages received by the listener |

**Send single — examples:**

```bash
# No body — defaults applied automatically
curl -X POST "http://localhost:8080/api/events/send/single"

# Type via query param
curl -X POST "http://localhost:8080/api/events/send/single?type=USER_REGISTERED"

# Full body
curl -X POST "http://localhost:8080/api/events/send/single" \
     -H "Content-Type: application/json" \
     -d '{"id":"abc-123","type":"ORDER_PLACED","payload":"hello","sequenceNumber":1}'
```

---

## Kafka Configuration

All Kafka config is explicit — no Spring Boot autoconfiguration (`spring.kafka.*` properties are used only for `bootstrap-servers`).

| Setting | Value | Why |
|---|---|---|
| Value serializer | `JacksonJsonSerializer` | Produces JSON bytes |
| Value deserializer | `JacksonJsonDeserializer` | Reads JSON into `EventMsg` |
| `TRUSTED_PACKAGES` | `*` | Allows deserialization of `com.vbforge.*` |
| `AUTO_OFFSET_RESET` | `earliest` | Consumer starts from beginning if no committed offset exists |
| `ENABLE_AUTO_COMMIT` | `false` | Manual ack control (Spring Kafka handles commit) |
| Producer `ACKS` | `all` | Strongest durability guarantee for tests |

---

## Key Concepts Demonstrated

| Concept | Where |
|---|---|
| `ConfluentKafkaContainer` (TC 2.x) | `AbstractKafkaIntegrationTest` |
| `@DynamicPropertySource` | Injects live broker address into Spring context |
| Shared container + cached context | One broker + one Spring context for all tests |
| `CountDownLatch` for async test synchronization | `EventConsumerService.resetLatch()` |
| `assign()` + `seek()` for offset-based isolation | `ProducerIntegrationTest.consumeFromOffset()` |
| `subscribe()` + unique group for full-topic scan | `ProducerIntegrationTest.consumeFromTopic()` |
| `@TestMethodOrder` for deterministic suite execution | All three test classes |
| Jackson `JavaTimeModule` for `LocalDateTime` round-trip | `AppConfig` |

---

## THEORY-Q-and-A-SECTION

**Q: Why use `static` for the Testcontainers `@Container` field?**  
A: A `static` field is created once per class loader, not once per test instance. Combined with `@Testcontainers` on the abstract base, the container starts before the first test and stops after the last. A non-static field would start a new container for every test method — dramatically slower.

**Q: Why does Spring only create one `ApplicationContext` for all three test classes?**  
A: Spring caches contexts by their configuration fingerprint. All three classes use `@SpringBootTest` with no custom configuration, and `@DynamicPropertySource` injects the same `bootstrap-servers` value (from the shared static container). Same fingerprint → same cached context → same beans, including the same `EventConsumerService`.

**Q: What is the difference between `assign()` and `subscribe()` in a Kafka consumer?**  
A: `subscribe(topic)` registers the consumer with a group coordinator and triggers a rebalance — the first `poll()` can take seconds. `assign(partition)` directly assigns a specific `TopicPartition` to the consumer, bypassing group coordination entirely. In tests, `assign()` + `seek(offset)` is faster and gives deterministic control over where reading starts.

**Q: Why does `resetLatch()` replace both the latch AND the list atomically?**  
A: The `@KafkaListener` runs on a background thread. If we replaced only the latch, a late-arriving message could still add itself to the old list, and a subsequent `getReceivedEvents()` in the new test would see unexpected entries. Replacing both under `synchronized` ensures the listener always writes into the collection that belongs to the current active latch.

**Q: What does `auto.offset.reset=earliest` mean, and why does it matter in tests?**  
A: When a consumer group has no committed offset for a partition (e.g. a freshly-created group ID), `earliest` tells the broker to start delivering from offset 0. In `consumeFromTopic()`, this means the raw consumer reads all messages ever produced to the topic. In `consumeFromOffset()` this setting is overridden by the explicit `seek()` call — `auto.offset.reset` only applies when there is no committed offset to fall back to.
