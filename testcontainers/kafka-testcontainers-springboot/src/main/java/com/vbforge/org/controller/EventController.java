package com.vbforge.org.controller;

import com.vbforge.org.model.EventMsg;
import com.vbforge.org.model.ProducerResponse;
import com.vbforge.org.service.EventConsumerService;
import com.vbforge.org.service.EventProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventConsumerService eventConsumerService;
    private final EventProducerService eventProducerService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {

        return ResponseEntity.ok("TESTCONTAINERS RUNNING!");
    }

    @PostMapping("/send/batch")
    public ResponseEntity<ProducerResponse> sendBatch(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "PLACED_ORDER") String type) {

        return ResponseEntity.ok(eventProducerService.sendBatch(count, type));
    }

    // JUNIOR NOTE: POST /send/single accepts an optional body to let callers supply a
    // full EventMsg. If no body is provided (or any fields are omitted), sensible
    // defaults are generated automatically — making this endpoint easy to call from
    // curl or Postman without a body:
    //
    //   curl -X POST "http://localhost:8080/api/events/send/single"
    //   curl -X POST "http://localhost:8080/api/events/send/single?type=USER_REGISTERED"
    //   curl -X POST "http://localhost:8080/api/events/send/single" \
    //        -H "Content-Type: application/json" \
    //        -d '{"id":"abc","type":"ORDER_PLACED","payload":"p1","sequenceNumber":1}'
    //
    // The response is 202 Accepted (not 200 OK) because the message is handed off to
    // Kafka asynchronously — the client should not assume it was processed, only queued.
    @PostMapping("/send/single")
    public ResponseEntity<Map<String, Object>> sendSingle(
            @RequestBody(required = false) EventMsg body,
            @RequestParam(required = false) String type) {

        EventMsg event = EventMsg.builder()
                .id(body != null && body.getId() != null ? body.getId() : UUID.randomUUID().toString())
                .type(body != null && body.getType() != null ? body.getType() : (type != null ? type : "MANUAL_EVENT"))
                .payload(body != null && body.getPayload() != null ? body.getPayload() : "manual-payload")
                .sequenceNumber(body != null ? body.getSequenceNumber() : 0)
                .createdAt(LocalDateTime.now())
                .build();

        eventProducerService.sendSingle(event);

        return ResponseEntity.accepted().body(Map.of(
                "messageId", event.getId(),
                "type", event.getType(),
                "sentAt", event.getCreatedAt().toString()
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {

        return ResponseEntity.ok(Map.of(
                "totalReceived", eventConsumerService.getTotalReceivedCounter(),
                "bufferedCount", eventConsumerService.getReceivedEvents().size()
        ));
    }


}
