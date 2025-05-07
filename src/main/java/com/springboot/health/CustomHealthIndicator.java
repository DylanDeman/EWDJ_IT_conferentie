package com.springboot.health;

import com.springboot.repository.EventRepository;
import com.springboot.repository.RoomRepository;
import com.springboot.repository.UserRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public CustomHealthIndicator(EventRepository eventRepository,
                               RoomRepository roomRepository,
                               UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Health health() {
        try {
            long eventCount = eventRepository.count();
            long roomCount = roomRepository.count();
            long userCount = userRepository.count();

            return Health.up()
                    .withDetail("events", eventCount)
                    .withDetail("rooms", roomCount)
                    .withDetail("users", userCount)
                    .withDetail("database", "connected")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
} 