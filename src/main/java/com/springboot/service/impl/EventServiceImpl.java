package com.springboot.service.impl;

import com.springboot.domain.Event;
import com.springboot.domain.User;
import com.springboot.repository.EventRepository;
import com.springboot.repository.UserRepository;
import com.springboot.service.EventService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Value("${app.favorites.limit:5}")
    private int favoritesLimit;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public void deleteById(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByDate(LocalDateTime date) {
        return eventRepository.findByDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getRoomCapacity(Long roomId) {
        return eventRepository.findRoomCapacity(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getUserFavorites(String username) {
        return eventRepository.findUserFavorites(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReachedFavoriteLimit(String username) {
        return eventRepository.countUserFavorites(username) >= favoritesLimit;
    }

    @Override
    public void addToFavorites(Long eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if (hasReachedFavoriteLimit(username)) {
            throw new IllegalStateException("Favorite limit reached");
        }
        
        event.getFavoritedBy().add(user);
        eventRepository.save(event);
    }

    @Override
    public void removeFromFavorites(Long eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        event.getFavoritedBy().remove(user);
        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAllByOrderByDateTimeAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByDate(LocalDateTime date) {
        return eventRepository.findByDateOrderByDateTimeAndName(date);
    }

    @Override
    public Event createEvent(Event event) {
        validateEvent(event);
        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(Long id, Event event) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        
        validateEvent(event);
        event.setId(id);
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(LocalDateTime dateTime, Long roomId) {
        return !eventRepository.existsByRoomIdAndDateTime(roomId, dateTime);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEventNameUniqueOnDate(LocalDateTime date, String name) {
        return !eventRepository.existsByNameAndDateTime(name, date);
    }

    private void validateEvent(Event event) {
        if (!event.validateBeamerCheck()) {
            throw new IllegalArgumentException("Invalid beamer check code");
        }
        
        if (!isRoomAvailable(event.getDateTime(), event.getRoom().getId())) {
            throw new IllegalArgumentException("Room is not available at the specified time");
        }
        
        if (!isEventNameUniqueOnDate(event.getDateTime(), event.getName())) {
            throw new IllegalArgumentException("Event name must be unique on the same date");
        }
    }
} 