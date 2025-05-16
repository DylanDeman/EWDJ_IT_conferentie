package com.springboot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.repository.EventRepository;
import com.springboot.repository.UserRepository;
import com.springboot.service.EventService;

import domain.Event;
import domain.User;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class EventServiceImpl implements EventService {

	@Value("${app.favorites.limit:5}")
	private int favoritesLimit;

	private final EventRepository eventRepository;
	private final UserRepository userRepository;

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
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		return user.getFavorites().stream().toList();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasReachedFavoriteLimit(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		return user.getFavorites().size() >= favoritesLimit;
	}

	@Override
	public void addToFavorites(Long eventId, String username) {
		if (eventId == null) {
			throw new IllegalArgumentException("Event ID is required");
		}

		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

		if (user.getFavorites().size() >= favoritesLimit) {
			throw new IllegalStateException("User has reached the maximum limit of " + favoritesLimit + " favorites");
		}

		user.getFavorites().add(event);
		userRepository.save(user);
	}

	@Override
	public void removeFromFavorites(Long eventId, String username) {
		if (eventId == null) {
			throw new IllegalArgumentException("Event ID is required");
		}

		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

		user.getFavorites().remove(event);
		userRepository.save(user);
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