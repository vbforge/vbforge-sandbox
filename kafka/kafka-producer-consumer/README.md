# kafka-producer-consumer
Producer/Consumer (Docker Kafka): callbacks for send confirmation, graceful shutdown

---

# SCENARIO: Simple Producer and Consumer (Docker Kafka)

**This scenario demonstrates:**
- Basic message production to a topic using Docker Kafka
- Basic message consumption from a topic
- Asynchronous callbacks for send confirmation
- Graceful shutdown handling
- Metrics and performance tracking

**Use Case:** Simple message queue with guaranteed order

---

## PREREQUISITES
- Docker Desktop installed and running
- Project built (`mvn clean compile`)

---

## HOW TO RUN THIS SCENARIO WITH DOCKER:

### Step 1: Start Kafka in Docker
```bash
# From project root directory
docker-compose up -d

# Verify Kafka is running
docker-compose ps

# Check Kafka logs (optional)
docker-compose logs -f kafka
```

### Step 2: Create the topic (if auto-create is disabled)
```bash
# Connect to Kafka container and create topic
docker exec -it kafka-explorer-broker kafka-topics \
  --create \
  --topic simple-topic \
  --partitions 1 \
  --replication-factor 1 \
  --bootstrap-server localhost:19092

# Verify topic was created
docker exec -it kafka-explorer-broker kafka-topics \
  --list \
  --bootstrap-server localhost:19092
```

**Note:** With `KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"` in docker-compose.yml, topics are auto-created when first message is sent. 
The manual creation above is optional but recommended for controlling partition count.

### Step 3: Run Consumer (Terminal 1)
> `SimpleConsumer`

**Expected Consumer Output:**
```
🚀 Starting SimpleConsumer (Docker Kafka)
✅ Subscribed to topic: simple-topic
⏳ Waiting for messages... (Press Ctrl+C to stop)
📊 Polling timeout: 1000ms
```

### Step 4: Run Producer (Terminal 2)
> `SimpleProducer`

**Expected Producer Output:**
```
🚀 Starting SimpleProducer (Docker Kafka)
📋 Configuration: 10 messages, 500ms delay
✅ Producer created successfully
📤 Sending 10 messages to topic: simple-topic
✅ Message #1 sent successfully (45ms)
✅ Message #2 sent successfully (38ms)
...
═══════════════════════════════════════════
📊 FINAL STATISTICS:
   Expected messages: 10
   Successfully sent: 10
   Failed: 0
   Total time: 5234 ms
   Throughput: 1.91 msgs/sec
✅ ALL MESSAGES SENT SUCCESSFULLY!
═══════════════════════════════════════════
```

### Step 5: how to run both by scripts in separate terminals (so Consumer could be terminated properly via `Ctrl + C`)
```bash
# make executable once
chmod +x run-consumer.sh run-producer.sh

# terminal 1 — start consumer
./run-consumer.sh

# terminal 2 — start producer
./run-producer.sh
```


### Step 6: Observe Consumer Output
The consumer will display each received message with metadata:
```
📦 Received batch of 10 messages
┌─────────────────────────────────────────────
│ 📨 Message #1
│    Value: Message #1 from Docker Kafka - Timestamp: 1734567890123
│    Partition: 0
│    Offset: 0
│    Timestamp: 1734567890123
│    Key: null
└─────────────────────────────────────────────
✅ Batch processed in 15ms (avg 1.50ms/msg)
```

### Step 7: Stop Consumer Gracefully
Press `Ctrl+C` in the consumer terminal. You'll see shutdown statistics:
```
🛑 Shutdown signal received
═══════════════════════════════════════════
📊 FINAL STATISTICS:
   Messages processed: 10
   Total runtime: 45678 ms
   Average throughput: 0.22 msgs/sec
═══════════════════════════════════════════
🏁 SimpleConsumer finished!
```

---

## OBSERVATIONS:

✅ **Message Order Guaranteed** - Messages appear in sequence (1, 2, 3...)
✅ **Single Partition** - All messages go to partition 0
✅ **Near Real-time** - Consumer receives messages immediately
✅ **Graceful Shutdown** - Consumer reports statistics before exiting
✅ **Metrics Tracking** - Performance metrics for both producer and consumer

---

## DOCKER KAFKA CLI COMMANDS (Useful for Debugging):

```bash
# List all topics
docker exec -it kafka-explorer-broker kafka-topics --list --bootstrap-server localhost:19092

# Describe topic (check partitions, replication)
docker exec -it kafka-explorer-broker kafka-topics --describe \
  --topic simple-topic \
  --bootstrap-server localhost:19092

# View messages from the beginning
docker exec -it kafka-explorer-broker kafka-console-consumer \
  --topic simple-topic \
  --bootstrap-server localhost:19092 \
  --from-beginning

# Check consumer group status
docker exec -it kafka-explorer-broker kafka-consumer-groups \
  --bootstrap-server localhost:19092 \
  --group consumer-group-simple \
  --describe

# Delete topic (cleanup)
docker exec -it kafka-explorer-broker kafka-topics \
  --delete \
  --topic simple-topic \
  --bootstrap-server localhost:19092
```

---

## TROUBLESHOOTING:

| Issue | Solution |
|-------|----------|
| Connection refused | Ensure Docker is running and `docker-compose up -d` was executed |
| Topic doesn't exist | Auto-create is enabled, or create manually using CLI commands above |
| Consumer not receiving messages | Check if producer ran successfully; verify topic has messages using console-consumer |
| Port conflicts (9092) | Stop local Kafka if running: `netstat -ano \| findstr :9092` |
| Shutdown hook not working | Use Ctrl+C, not the IDE stop button |

---

## CLEANUP:

```bash
# Stop Kafka and remove containers
docker-compose down

# Remove volumes (resets Kafka data)
docker-compose down -v

# Delete topic (if needed for fresh start)
docker exec -it kafka-explorer-broker kafka-topics \
  --delete \
  --topic simple-topic \
  --bootstrap-server localhost:19092
```

---

## 📈 Summary

| Aspect | Operational concerns                   |
|--------|----------------------------------------|
| **Shutdown** | Graceful with ShutdownHook             |
| **Metrics** | Throughput, timing, success rates      |
| **Error Handling** | Fine-grained with recovery             |
| **Logging** | DEBUG for high-volume, structured logs |
| **Configuration** | Environment-aware, configurable        |
| **Docker Support** | Complete Docker instructions           |
| **Production Features** | Idempotent, compression, timeouts      |

---