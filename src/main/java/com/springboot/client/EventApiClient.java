package com.springboot.client;

import com.springboot.domain.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventApiClient {

    private final WebClient webClient;

    public EventApiClient(@Value("${app.api.base-url:http://localhost:8080}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<List<Event>> getEventsByDate(LocalDateTime date) {
        return webClient.get()
                .uri("/api/events/date/{date}", date)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Event>>() {});
    }

    public Mono<Integer> getRoomCapacity(Long roomId) {
        return webClient.get()
                .uri("/api/events/room/{roomId}/capacity", roomId)
                .retrieve()
                .bodyToMono(Integer.class);
    }
} 