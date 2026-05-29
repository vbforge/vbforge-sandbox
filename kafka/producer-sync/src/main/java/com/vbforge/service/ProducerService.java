package com.vbforge.service;

import com.vbforge.model.MyMessageObject;
import com.vbforge.model.SendResultMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.sync}")
    private String topic;

    @Value("${kafka.default.message}")
    private String defaultMessage;

    @Value("${kafka.producer.send-timeout-seconds}")
    private int sendTimeoutSeconds;

    // 1. basic blocking: .get() - no timeout
    public SendResultMetadata sendBlocking(String content) {

        //build message to be send
        MyMessageObject myMessageObject = buildMyMessageObject(content);

        log.info("Thread will block until ACK");
        log.info("Sending message with ID: {}", myMessageObject.getId());

        //starter time
        long start = System.currentTimeMillis();

        // try with catch blocks where only two Exceptions handling: InterruptedException, ExecutionException
        // (since we do not have any timeouts in this method - we not catch TimeOutException)

        try{
            //get sent result (SendResult - is a Kafka support class)
            //.get() — no timeout. Thread blocks here until broker responds.
            SendResult<String, Object> sendResult = kafkaTemplate
                    .send(topic, myMessageObject.getId(), myMessageObject)
                    .get(); //block here indefinitely

            // calculation time for sending operation has been taken
            long duration = System.currentTimeMillis() - start;

            //from SendResult we can get a Record Metadata
            RecordMetadata recordMetadata = sendResult.getRecordMetadata();
            log.info("ACK received in {}ms | partition={} offset={}",  duration, recordMetadata.partition(), recordMetadata.offset());

            //build our custom send result object
             return buildSendResult(myMessageObject, recordMetadata, duration);

        //first exception we have to handle
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sending was interrupted while waiting for broker ACK", e);
        }
        //second exception we have to handle
        catch (ExecutionException e){
            //wrap the actual Kafka error, could be unwrapped into a specific Exceptions to get real cause: TopicAuthorizationException, NetworkException
            throw new RuntimeException("Broker rejected the message: " + e.getCause().getMessage(), e);
        }

    }

    // 2. sent with timeout: Blocking send WITH timeout — the production-safe pattern
    public SendResultMetadata sendWithTimeout(String content) {

        MyMessageObject myMessageObject = buildMyMessageObject(content);
        log.info("Thread will wait max {}s for ACK",  sendTimeoutSeconds);
        log.info("Sending message with ID: {}", myMessageObject.getId());

        long start = System.currentTimeMillis();

        try{
            SendResult<String, Object> result = kafkaTemplate
                    .send(topic, myMessageObject.getId(), myMessageObject)
                    .get(sendTimeoutSeconds, TimeUnit.SECONDS); // bounded blocking

            long duration = System.currentTimeMillis() - start;
            RecordMetadata recordMetadata = result.getRecordMetadata();
            log.info("[TIMEOUT]: ACK received in {}ms | partition={} offset={}", duration, recordMetadata.partition(), recordMetadata.offset());

            return  buildSendResult(myMessageObject, recordMetadata, duration);

        }catch (TimeoutException e){
            // TimeoutException here is java.util.concurrent.TimeoutException,
            // NOT Kafka's own TimeoutException. The Future's .get() threw this because
            // the broker didn't respond within sendTimeoutSeconds.
            // At this point the message MAY or MAY NOT have been written to Kafka —
            // we simply don't know. This is a key operational concern in sync sends.
            long duration = System.currentTimeMillis() - start;
            log.info("[TIMEOUT]: No ACK after {}ms — broker too slow or unreachable", duration);
            throw new RuntimeException("Kafka send timed out after " + sendTimeoutSeconds + "s — broker did not ACK", e);

        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Send interrupted while waiting for broker ACK", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Broker rejected the message: " + e.getCause().getMessage(), e);
        }
    }

    // 3. sync send with custom timeout passed by the caller
    public SendResultMetadata sendWithCustomTimeout(String content, int sendTimeoutSeconds) {

        MyMessageObject myMessageObject = buildMyMessageObject(content);
        log.info("Thread will wait max with custom timeout: {}s for ACK",  sendTimeoutSeconds);
        log.info("Sending message with ID: {}", myMessageObject.getId());
        long start = System.currentTimeMillis();

        try{
            SendResult<String, Object> result = kafkaTemplate
                    .send(topic, myMessageObject.getId(), myMessageObject)
                    .get(sendTimeoutSeconds, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - start;
            RecordMetadata meta = result.getRecordMetadata();

            log.info("[CUSTOM-TIMEOUT]: ACK received in {}ms | partition={} offset={}", duration, meta.partition(), meta.offset());

            return buildSendResult(myMessageObject, meta, duration);

        }catch (TimeoutException e) {
            long duration = System.currentTimeMillis() - start;
            log.error("[CUSTOM-TIMEOUT]: No ACK after {}ms (custom timeout={}s)", duration, sendTimeoutSeconds);
            throw new RuntimeException("Kafka send timed out after " + sendTimeoutSeconds + "s", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Send interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Broker error: " + e.getCause().getMessage(), e);
        }

    }

    //helper methods

    //to build message object with content
    private MyMessageObject buildMyMessageObject(String content) {
        if (content == null || content.isBlank()) {
            content = defaultMessage;
        }
        return MyMessageObject.builder()
                .id(UUID.randomUUID().toString())
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }

    //to build metadata of what has been sent
    private SendResultMetadata buildSendResult(MyMessageObject myMessageObject, RecordMetadata recordMetadata, long durationsMs) {
        return SendResultMetadata.builder()
                .myMessageObject(myMessageObject)
                .partition(recordMetadata.partition())
                .offset(recordMetadata.offset())
                .brokerTimestamp(recordMetadata.timestamp())
                .sendDurationMs(durationsMs)
                .respondedAt(LocalDateTime.now())
                .build();
    }




}
