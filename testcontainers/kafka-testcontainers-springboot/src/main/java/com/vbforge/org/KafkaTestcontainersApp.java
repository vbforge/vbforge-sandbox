package com.vbforge.org;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class KafkaTestcontainersApp {

    public static void main(String[] args) {
        SpringApplication.run(KafkaTestcontainersApp.class, args);
    }

}
