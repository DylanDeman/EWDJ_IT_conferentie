package service;

import domain.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.ui.Model;

public interface EventService {
    Optional<Event> findById(Long id);
    List<Event> findAll();
    Event save(Event event);
    void deleteById(Long id);

    List<Event> getEventsByDate(LocalDate formattedDate);
}