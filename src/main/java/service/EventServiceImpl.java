package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import domain.Event;
import domain.MyUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import repository.EventRepository;
import repository.UserRepository;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

	@Value("${app.favorites.limit:5}")
	private int favoritesLimit;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public List<Event> findAll() {
		return eventRepository.findAll();
	}

	@Override
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
	public List<Event> findByDate(LocalDateTime date) {
		return eventRepository.findByDate(date);
	}

	@Override
	public Optional<Integer> getRoomCapacity(Long roomId) {
		return eventRepository.findRoomCapacity(roomId);
	}

	@Override
	public List<Event> getUserFavorites(String username) {
		MyUser user = userRepository.findByUsername(username);
		return user.getFavorites().stream().toList();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasReachedFavoriteLimit(String username) {
		MyUser user = userRepository.findByUsername(username);
		return user.getFavorites().size() >= favoritesLimit;
	}

	@Override
	public void addToFavorites(Long eventId, String username) {
		if (eventId == null) {
			throw new IllegalArgumentException("Event ID is required");
		}

		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

		MyUser user = userRepository.findByUsername(username);

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

		MyUser user = userRepository.findByUsername(username);
		user.getFavorites().remove(event);
		userRepository.save(user);
	}

	@Override
	public List<Event> getAllEvents() {
		return eventRepository.findAllByOrderByDateTimeAsc();
	}

	@Override
	public Optional<Event> getEventById(Long id) {
		return eventRepository.findById(id);
	}

	@Override
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
		if (!eventRepository.existsById(id)) {
			throw new EntityNotFoundException("Event not found");
		}

		log.info("Updating event ID {}: {}", id, event);
		validateEventUpdate(id, event);
		event.setId(id);
		return eventRepository.save(event);
	}

	@Override
	public void deleteEvent(Long id) {
		eventRepository.deleteById(id);
	}

	@Override
	public boolean isRoomAvailable(LocalDateTime dateTime, Long roomId) {
		return !eventRepository.existsByRoomIdAndDateTime(roomId, dateTime);
	}

	@Override
	public boolean isEventNameUniqueOnDate(LocalDateTime date, String name) {
		return !eventRepository.existsByNameAndDateTime(name, date);
	}

	private void validateEvent(Event event) {
		if (event.getBeamerCheck() != event.getBeamerCode() % 97) {
			throw new IllegalArgumentException("Invalid beamer check code");
		}

		if (!isRoomAvailable(event.getDateTime(), event.getRoom().getId())) {
			throw new IllegalArgumentException("Room is not available at the specified time");
		}

		if (!isEventNameUniqueOnDate(event.getDateTime(), event.getName())) {
			throw new IllegalArgumentException("Event name must be unique on the same date");
		}
	}

	private void validateEventUpdate(Long eventId, Event event) {
		if (event.getBeamerCheck() != event.getBeamerCode() % 97) {
			throw new IllegalArgumentException("Invalid beamer check code");
		}

		Event existing = eventRepository.findById(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found"));

		boolean timeOrRoomChanged = !existing.getDateTime().equals(event.getDateTime())
				|| existing.getRoom().getId() != event.getRoom().getId();

		if (timeOrRoomChanged && !isRoomAvailable(event.getDateTime(), event.getRoom().getId())) {
			throw new IllegalArgumentException("Room is not available at the specified time");
		}

		boolean nameChanged = !existing.getName().equalsIgnoreCase(event.getName());

		if (nameChanged && !isEventNameUniqueOnDate(event.getDateTime(), event.getName())) {
			throw new IllegalArgumentException("Event name must be unique on the same date");
		}
	}

	@Override
	public List<Event> getFilteredEvents(String dateStr, Long roomId, String sortBy) {
		List<Event> filteredEvents = new ArrayList<>(getAllEvents());

		if (dateStr != null && !dateStr.isEmpty()) {
			try {
				LocalDate filterDate = LocalDate.parse(dateStr);
				filteredEvents = filteredEvents.stream().filter(event -> {
					LocalDate eventDate = event.getDateTime().toLocalDate();
					return eventDate.equals(filterDate);
				}).collect(Collectors.toList());
			} catch (DateTimeParseException e) {
				log.warn("Invalid date format for filtering: {}", dateStr);
			}
		}

		if (roomId != null) {
			filteredEvents = filteredEvents.stream()
					.filter(event -> Long.valueOf(event.getRoom().getId()).equals(roomId)).collect(Collectors.toList());
		}

		switch (sortBy.toLowerCase()) {
		case "name":
			filteredEvents.sort(Comparator.comparing(Event::getName));
			break;
		case "price":
			filteredEvents.sort(Comparator.comparing(Event::getPrice));
			break;
		case "datetime":
		default:
			filteredEvents.sort(Comparator.comparing(Event::getDateTime));
			break;
		}

		return filteredEvents;
	}
}
