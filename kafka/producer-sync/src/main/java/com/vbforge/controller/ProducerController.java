package com.vbforge.controller;

import com.vbforge.model.SendResultMetadata;
import com.vbforge.service.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/producer")
@RequiredArgsConstructor
@Slf4j
public class ProducerController {

    private final ProducerService producerService;

    //POST http://localhost:8080/api/producer/send-blocking?content=Hello from Kafka (send-blocking)!
    //1. blocking as it most basic meaning: .get() - no timeout
    @PostMapping("/send-blocking")
    public ResponseEntity<SendResultMetadata> sendBlocking(@RequestParam(required = false) String content){

        log.info(">>> /send-blocking");

        SendResultMetadata resultMetadata = producerService.sendBlocking(content);

        return ResponseEntity.ok(resultMetadata);
    }

    //POST http://localhost:8080/api/producer/send-with-timeout?content=Hello from Kafka (send-with-timeout)!
    //2. Bounded blocking — .get(timeout, unit) from application.yml config
    @PostMapping("/send-with-timeout")
    public ResponseEntity<SendResultMetadata> sendWithTimeout(@RequestParam(required = false) String content){

        log.info(">>> /send-with-timeout");

        SendResultMetadata resultMetadata = producerService.sendWithTimeout(content);

        return ResponseEntity.ok(resultMetadata);
    }

    //POST http://localhost:8080/api/producer/send-with-custom-timeout?content=Hello from Kafka (send-with-custom-timeout)!
    //3. Caller-controlled timeout — flexible per use-case (different message priorities)
    @PostMapping("/send-with-custom-timeout")
    public ResponseEntity<SendResultMetadata> sendWithCustomTimeout(
            @RequestParam(required = false) String content,
            @RequestParam(defaultValue = "3") int timeoutSeconds){

        log.info(">>> /send-with-custom-timeout called with timeoutSeconds: {}", timeoutSeconds);

        SendResultMetadata resultMetadata = producerService.sendWithCustomTimeout(content, timeoutSeconds);

        return ResponseEntity.ok(resultMetadata);
    }


}












