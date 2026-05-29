package com.vbforge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"topic-sync"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class ProducerSyncApplicationTests {

    @Test
    void contextLoads() {
    }

}
