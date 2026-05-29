package com.vbforge.service;

import com.vbforge.model.MyMessageObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsumerService {

    @KafkaListener(
            topics = "${kafka.topic.sync}",
            groupId = "${kafka.consumer.groupID}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(MyMessageObject message) {

        log.info("****** Message Received *****");
        log.info(" * ID:        {}", message.getId());
        log.info(" * Content:   {}", message.getContent());
        log.info(" * Timestamp: {}", message.getTimestamp());
        log.info("******************************");

    }


}
