package com.vbforge.org.service;

import com.vbforge.org.model.EventMsg;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j

public class EventConsumerService {

    private final AtomicLong totalReceivedCounter = new AtomicLong(0);
    private volatile CopyOnWriteArrayList<EventMsg> receivedEvents = new CopyOnWriteArrayList<>();
    private volatile CountDownLatch latch = new CountDownLatch(0);

    //CONSUME
    @KafkaListener(
            topics = "${kafka.topics.events}",
            groupId = "${kafka.consumer.groupID}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(final EventMsg eventMsg) {
        totalReceivedCounter.incrementAndGet();
        final CopyOnWriteArrayList<EventMsg> currentList = this.receivedEvents;
        final CountDownLatch currentLatch = this.latch;

        currentList.add(eventMsg);
        currentLatch.countDown();

        log.debug("[CONSUMER] Received id={}, type={}, seq={}", eventMsg.getId(), eventMsg.getType(), eventMsg.getSequenceNumber());
    }

    //HELPERS FOR TESTING
    public synchronized void resetLatch(int expectedCount) {
        receivedEvents = new CopyOnWriteArrayList<>();
        latch = new CountDownLatch(expectedCount);
        log.debug("[CONSUMER] resetLatch({}) — fresh list and latch installed", expectedCount);
    }


    public CountDownLatch getLatch() {
        return latch;
    }

    public List<EventMsg> getReceivedEvents() {
        return Collections.unmodifiableList(receivedEvents);
    }

    public long getTotalReceivedCounter() {
        return totalReceivedCounter.get();
    }


}
