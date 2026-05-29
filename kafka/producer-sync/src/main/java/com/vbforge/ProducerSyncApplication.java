package com.vbforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ProducerSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducerSyncApplication.class, args);
    }

}
