package com.vbforge.org.service;

import com.vbforge.org.model.EventMsg;
import com.vbforge.org.model.ProducerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.events}")
    private String eventTopic;

    //sending batch messages at ones
    public ProducerResponse sendBatch(int count, String type){
        List<ProducerResponse.MessageSummary> summaries = new ArrayList<>();
        log.info("Sending Batch Events to Topic: {} [count: {}, type: {}]", eventTopic, count, type);

        //loop for sending
        for(int i = 1; i <= count; i++){
            EventMsg event = EventMsg.builder()
                    .id(UUID.randomUUID().toString())
                    .type(type)
                    .payload("Payload for event: " + i)
                    .sequenceNumber(i)
                    .createdAt(LocalDateTime.now())
                    .build();

            try{
                SendResult<String, Object> result = kafkaTemplate.send(eventTopic, event.getId(), event).get(5, TimeUnit.SECONDS);
                RecordMetadata meta = result.getRecordMetadata();
                summaries.add(ProducerResponse.MessageSummary.builder()
                        .messageId(event.getId())
                        .sequenceNumber(i)
                        .partition(meta.partition())
                        .offset(meta.offset())
                        .build());

            }catch (TimeoutException e) {
                throw new RuntimeException("Send timed out at event #" + i, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Send interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Broker error: " + e.getCause().getMessage(), e);
            }
        }

        //return statement
        log.info("Producer sent: {} events", summaries.size());
        return ProducerResponse.builder()
                .messageSent(summaries.size())
                .messages(summaries)
                .sentAt(LocalDateTime.now())
                .build();
    }


    //sending single message at ones
    public void sendSingle(EventMsg event){
        try{
            kafkaTemplate.send(eventTopic, event.getId(), event).get(5, TimeUnit.SECONDS);
            log.info("Sending Single Event with ID: {}", event.getId());
        }catch (Exception e){
            throw new RuntimeException("Failed to send event: " + e.getMessage(), e);
        }
    }




}


















