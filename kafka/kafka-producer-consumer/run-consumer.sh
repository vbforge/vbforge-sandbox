#!/bin/bash
# Run SimpleConsumer
# Stop with Ctrl+C — triggers ShutdownHook correctly

cd "$(dirname "$0")"

mvn exec:java \
  -Dexec.mainClass="com.vbforge.consumer.SimpleConsumer" \
  -DKAFKA_BOOTSTRAP_SERVERS=localhost:9092
