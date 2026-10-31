package com.goodcycle.disposal.event;

import java.time.Instant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodcycle.disposal.model.DisposalRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DisposalEventPublisher {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;

    public DisposalEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper objectMapper) {
        this.kafka = kafka;
        this.objectMapper = objectMapper;
    }

    public void publish(DisposalRequest request) {
        Event event = new Event(
                request.getId(),
                request.getStatus().name(),
                request.getUpdatedAt());
        try {
            kafka.send("disposal.events", request.getId(), objectMapper.writeValueAsString(event)).join();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize disposal lifecycle event.", e);
        }
    }

    public record Event(String requestId, String status, Instant occurredAt) {}
}
