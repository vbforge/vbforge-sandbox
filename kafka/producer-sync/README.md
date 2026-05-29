# producer-sync
Synchronous Kafka producer communication with blocking sends, timeout handling, retries, acknowledgments, and RecordMetadata inspection. (Docker Kafka)

| Property | Value                                 |
|----------|---------------------------------------|
| **Java** | 21                                    |
| **Spring Boot** | 4.0.6                                 |
| **Kafka** | cp-kafka 8.0.0 (KRaft mode) in Docker |
| **App port** | 8080                                  |

---

## What This Case Covers

- **Blocking send** with `Future.get()` — thread halts until broker ACK
- **Bounded blocking** with `Future.get(timeout, unit)` — the correct production pattern
- **Caller-controlled timeout** — different priorities, different deadlines
- `RecordMetadata` inspection — partition, offset, broker timestamp returned in response
- `ExecutionException` vs `TimeoutException` — two distinct failure modes
- `ACKS_CONFIG`, `REQUEST_TIMEOUT_MS_CONFIG`, `RETRIES_CONFIG` — producer config knobs
- Why `RETRIES=0` makes sense in a learning environment (want to see failures immediately, not have Kafka silently retry and mask them)

---

## Testing scenarios:

### Scenario 1: Normal Flow — Success with ACK 
- **Goal:** Verify that a message is successfully sent, the broker acknowledges it, and RecordMetadata is returned.
- `POST http://localhost:8080/api/producer/send-blocking?content=Hello from Kafka (send-blocking)!`

Expected output:
```
>>> /send-blocking
Thread will block until ACK
Sending message with ID: 59894d35-d71d-4959-8010-052dca835910
ACK received in 23ms | partition=0 offset=31
****** Message Received *****
 * ID:        59894d35-d71d-4959-8010-052dca835910
 * Content:   Hello from Kafka (send-blocking)!
 * Timestamp: 2026-05-29T19:35:24.286385400
******************************
```

**What happens under the hood:**
1) Producer creates MyMessageObject with unique ID
2) kafkaTemplate.send() returns ListenableFuture<SendResult>
3) .get() blocks the thread until broker ACK arrives
4) SendResult provides RecordMetadata with partition, offset, timestamp
5) Custom SendResultMetadata is returned to the client

```text
.get() with no timeout arguments blocks INDEFINITELY.
This is dangerous in production — if Kafka is slow or down, your HTTP request thread hangs forever (or until the server kills it).
We expose this in the API so you can see what "naked blocking" looks like.
In reality: NEVER use this in production. Always pass a timeout.
```

---

### Scenario 2: Bounded Timeout — Production-Safe Blocking

- **Goal:** Demonstrate the correct production pattern — waiting for broker ACK but with an upper time bound so the thread never hangs indefinitely.
- **Endpoint:** `POST http://localhost:8080/api/producer/send-with-timeout?content=Hello from Kafka (send-with-timeout)!`
- **Timeout configured:** `${kafka.producer.send-timeout-seconds}=5` (5 seconds max wait)

**Expected output (normal case — ACK arrives within timeout):**
```
>>> /send-with-timeout
Thread will wait max 5s for ACK
Sending message with ID: dfdd388d-e8ca-44c3-af42-1fef226541bb
[TIMEOUT]: ACK received in 11ms | partition=0 offset=35
****** Message Received *****
 * ID:        dfdd388d-e8ca-44c3-af42-1fef226541bb
 * Content:   Hello from Kafka (send-with-timeout)!
 * Timestamp: 2026-05-29T19:44:17.011264
******************************
```

**Expected output (timeout scenario — broker silent for 5+ seconds):**
```
>>> /send-with-timeout
Thread will wait max 5s for ACK
Sending message with ID: a1b2c3d4-5678-90ab-cdef-1234567890ab
[TIMEOUT]: No ACK after 5012ms — broker too slow or unreachable
ERROR: Kafka send timed out after 5s — broker did not ACK
```

**What happens under the hood:**
1) Producer creates `MyMessageObject` with unique ID
2) `kafkaTemplate.send()` returns `ListenableFuture<SendResult>`
3) `.get(5, TimeUnit.SECONDS)` blocks but **only for max 5 seconds**
4) If ACK arrives in time → proceed normally with `RecordMetadata`
5) If no ACK after 5 seconds → `TimeoutException` is thrown immediately

**Critical distinction from Scenario 1:**

| Aspect | Scenario 1 (`.get()`) | Scenario 2 (`.get(timeout)`) |
|--------|----------------------|------------------------------|
| **Max wait time** | Infinite (forever) | Fixed (5 seconds) |
| **Thread safety** | ❌ Dangerous | ✅ Production-ready |
| **Exception on timeout** | Never occurs | `TimeoutException` |
| **Use case** | Learning only | Real applications |

```text
⚠️ IMPORTANT: On TimeoutException, we DON'T know if the message was written to Kafka!
The broker might have:
  a) Never received it (network issue)
  b) Received but was too slow to ACK (overloaded)
  c) Written successfully but ACK got lost

This is why idempotent producers and unique message IDs matter — 
consumers must handle possible duplicates after timeouts.
```

**Why this pattern is production-safe:**
- HTTP thread won't hang forever if Kafka is down
- can return a meaningful error to the client within seconds
- Thread pool won't exhaust due to stuck requests
- Timeout value can be tuned per business requirement

**When to increase/decrease timeout:**
- **Lower timeout (1-2s):** Fast failures for real-time UI, health checks
- **Higher timeout (10-30s):** Batch jobs, non-critical async processing
- **Very high timeout (60s+):** Rare — usually indicates infrastructure problem

**Comparison between Scenario 1 and 2:**

```
Scenario 1 (.get()):
Thread → [---BLOCKS FOREVER---] → (never returns if broker dead)
         ↑
         DANGEROUS — thread leak!

Scenario 2 (.get(5, SECONDS)):
Thread → [--wait max 5s--] → ACK (23ms) ✓
         or
Thread → [--wait max 5s--] → TIMEOUT (5000ms) ✗
         ↑
         SAFE — thread always unblocks
```

---

### Scenario 3: Custom Timeout — Caller-Controlled Deadlines

- **Goal:** Demonstrate per-request timeout control — different API clients can specify their own timeout based on their SLA requirements.
- **Endpoint:** `POST http://localhost:8080/api/producer/send-with-custom-timeout?content=Hello from Kafka (send-with-custom-timeout)!&timeoutSeconds=5`
- **Timeout passed:** Caller decides — 2 seconds in this example (could be 1s for real-time UI or 30s for batch jobs)

**Expected output (normal case — ACK arrives within custom timeout):**
```
>>> /send-with-custom-timeout called with timeoutSeconds: 5
Thread will wait max with custom timeout: 5s for ACK
Sending message with ID: 8eda034d-f82a-43fd-b957-99a3a5a03c09
[CUSTOM-TIMEOUT]: ACK received in 11ms | partition=0 offset=36
****** Message Received *****
 * ID:        8eda034d-f82a-43fd-b957-99a3a5a03c09
 * Content:   Hello from Kafka (send-with-custom-timeout)!
 * Timestamp: 2026-05-29T19:44:51.963878
******************************
```

**Expected output (timeout scenario — 2 seconds too short for slow broker):**
```
>>> /send-with-custom-timeout?content=Hello!&timeoutSeconds=2
Thread will wait max with custom timeout: 2s for ACK
Sending message with ID: 1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d
[CUSTOM-TIMEOUT]: No ACK after 2003ms (custom timeout=2s)
ERROR: Kafka send timed out after 2s
```

**What happens under the hood:**

1) Producer creates `MyMessageObject` with unique ID
2) `kafkaTemplate.send()` returns `ListenableFuture<SendResult>`
3) `.get(timeoutSeconds, TimeUnit.SECONDS)` uses the **caller-provided** timeout value
4) Different from Scenario 2 — timeout isn't fixed in config, it's dynamic per request
5) Each caller can specify their own deadline based on business priority

**How this differs from Scenario 2:**

| Aspect | Scenario 2 (`sendWithTimeout`) | Scenario 3 (`sendWithCustomTimeout`) |
|--------|-------------------------------|--------------------------------------|
| **Timeout source** | Fixed in `application.yml` | Passed as method parameter |
| **Flexibility** | ❌ Same for all callers | ✅ Per-request control |
| **Use case** | One-size-fits-all timeout | Different SLAs per client |
| **API design** | No parameter needed | Caller provides timeout |

```text
🎯 REAL-WORLD USE CASE EXAMPLES:

┌─────────────────────────────────────────────────────────────────┐
│  Critical Payment Processing (5s timeout)                       │
│  POST /api/producer/send-with-custom-timeout?timeoutSeconds=5   │
│  → Must know success/failure quickly, can't leave user waiting  │
├─────────────────────────────────────────────────────────────────┤
│  Analytics Logging (30s timeout)                                │
│  POST /api/producer/send-with-custom-timeout?timeoutSeconds=30  │
│  → Can tolerate longer waits, less business critical            │
├─────────────────────────────────────────────────────────────────┤
│  Health Check (1s timeout)                                      │
│  POST /api/producer/send-with-custom-timeout?timeoutSeconds=1   │
│  → Fast failure to detect broker issues immediately             │
├─────────────────────────────────────────────────────────────────┤
│  Batch Processing (60s timeout)                                 │
│  POST /api/producer/send-with-custom-timeout?timeoutSeconds=60  │
│  → Background job, willing to wait longer for success           │
└─────────────────────────────────────────────────────────────────┘
```

**Important considerations for custom timeouts:**

```text
⚠️ VALIDATION NEEDED IN PRODUCTION:

public SendResultMetadata sendWithCustomTimeout(String content, int sendTimeoutSeconds) {
    // Protect against abuse
    if (sendTimeoutSeconds < 1) {
        throw new IllegalArgumentException("Timeout must be at least 1 second");
    }
    if (sendTimeoutSeconds > 60) {
        throw new IllegalArgumentException("Timeout cannot exceed 60 seconds");
    }
    // ... rest of method
}
```

**Why custom timeouts matter:**

- **User experience:** Real-time UI needs fast failure (2-3s max)
- **Cost control:** Prevent one slow client from exhausting thread pool
- **Multi-tenancy:** VIP customers get higher timeouts than free tier
- **Degraded mode:** Reduce timeouts when system is under stress
- **Testing:** Easily simulate timeout behavior without reconfiguring

**Testing the timeout boundary:**

```bash
# Should succeed (normal broker response within 5 seconds)
curl -X POST "http://localhost:8080/api/producer/send-with-custom-timeout?content=Hello from Kafka (send-with-custom-timeout)!&timeoutSeconds=5"
# POST 

# Should timeout (if broker takes >1 second to ACK)
curl -X POST "http://localhost:8080/api/producer/send-with-custom-timeout?content=Hello from Kafka (send-with-custom-timeout)!&timeoutSeconds=1"

# Should be rejected by validation (if implemented)
curl -X POST "http://localhost:8080/api/producer/send-with-custom-timeout?content=Hello from Kafka (send-with-custom-timeout)!&timeoutSeconds=0"
```

---

## 📊 Complete Comparison of All Three Scenarios

| Scenario | Method | Timeout Source | Exception on Timeout | Production Ready |
|----------|--------|----------------|---------------------|------------------|
| **1** | `sendBlocking()` | None (infinite) | Never | ❌ No |
| **2** | `sendWithTimeout()` | Fixed (`application.yml`) | `TimeoutException` | ✅ Yes (simple) |
| **3** | `sendWithCustomTimeout()` | Per-request parameter | `TimeoutException` | ✅ Yes (advanced) |


**The Three Timeout Patterns Side-by-Side:**

```
Scenario 1: .get()
├── Timeout: INFINITE
├── Risk: Thread leak if broker down
└── Use: LEARNING ONLY ❌

Scenario 2: .get(fixed, SECONDS)
├── Timeout: 5s (from config)
├── Risk: Same for all callers
└── Use: SIMPLE PRODUCTION ✅

Scenario 3: .get(custom, SECONDS)  
├── Timeout: Caller decides (1-60s)
├── Risk: Caller might set unrealistic timeout
└── Use: ADVANCED / MULTI-TENANT ✅
```
