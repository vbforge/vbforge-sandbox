#!/bin/bash
# Run SimpleProducer
# Exits automatically after sending all messages

cd "$(dirname "$0")"

mvn exec:java \
  -Dexec.mainClass="com.vbforge.producer.SimpleProducer" \
  -DKAFKA_BOOTSTRAP_SERVERS=localhost:9092
