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
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

import domain.Event;
import domain.MyUser;
import domain.Room;
import domain.Speaker;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import repository.EventRepository;
import repository.RoomRepository;
import repository.SpeakerRepository;
import repository.UserRepository;
import util.Role;
import validation.EventValidator;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

	@Value("${app.favorites.limit:5}")
	private int favoritesLimit;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private SpeakerRepository speakerRepository;

	@Autowired
	private EventValidator eventValidator;

	@Override
	public List<Event> getAllEvents() {
		return eventRepository.findAllByOrderByDateTimeAsc();
	}

	@Override
	public Optional<Event> getEventById(Long id) {
		return eventRepository.findById(id);
	}

	@Override
	public Event createEvent(Event event) {
		Errors errors = new BeanPropertyBindingResult(event, "event");
		eventValidator.validate(event, errors);

		if (errors.hasErrors()) {
			String errorMsg = errors.getFieldErrors().get(0).getDefaultMessage();
			throw new IllegalArgumentException(errorMsg);
		}

		return eventRepository.save(event);
	}

	@Override
	public Event updateEvent(Long id, Event event) {
		if (!eventRepository.existsById(id)) {
			throw new EntityNotFoundException("Event not found");
		}

		event.setId(id);

		Errors errors = new BeanPropertyBindingResult(event, "event");
		eventValidator.validate(event, errors);

		if (errors.hasErrors()) {
			String errorMsg = errors.getFieldErrors().get(0).getDefaultMessage();
			throw new IllegalArgumentException(errorMsg);
		}

		log.info("Updating event ID {}: {}", id, event);
		return eventRepository.save(event);
	}

	@Override
	public void deleteEvent(Long id) {
		eventRepository.deleteById(id);
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

	@Override
	public List<Event> getEventsByDate(LocalDateTime date) {
		return eventRepository.findByDateOrderByDateTimeAndName(date);
	}

	@Override
	public boolean isRoomAvailable(LocalDateTime dateTime, Long roomId) {
		return eventValidator.isRoomAvailable(dateTime, roomId);
	}

	@Override
	public boolean isEventNameUniqueOnDate(LocalDateTime date, String name) {
		return eventValidator.isEventNameUniqueOnDate(date, name);
	}

	@Override
	public boolean existsByNameAndDate(String name, LocalDate date) {
		return eventValidator.existsByNameAndDate(name, date);
	}

	@Override
	public boolean existsByNameAndDateExcludingId(String name, LocalDate date, Long eventId) {
		return eventValidator.existsByNameAndDateExcludingId(name, date, eventId);
	}

	@Override
	public void validateEvent(Event event, BindingResult result) {
		eventValidator.validate(event, result);
	}

	@Override
	public List<Room> setupRooms() {
		return roomRepository.findAll();
	}

	@Override
	public List<Speaker> setupSpeakers() {
		return speakerRepository.findAll();
	}

	@Override
	public Optional<Integer> getRoomCapacity(Long roomId) {
		return eventRepository.findRoomCapacity(roomId);
	}

	@Override
	public Event createEmptyEvent() {
		Event event = new Event();
		event.setSpeakers(new ArrayList<>());
		return event;
	}

	@Override
	public void populateEventFormModel(Model model, Event event) {
		model.addAttribute("rooms", setupRooms());
		model.addAttribute("allSpeakers", setupSpeakers());
		addSpeakerSelectionAttributes(model, event);
	}

	@Override
	public void prepareUserEventContext(Model model, String username, Long eventId) {
		MyUser user = userRepository.findByUsername(username);
		boolean isAdmin = user.getRole() == Role.ADMIN;

		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("canAddToFavorites", !hasReachedFavoriteLimit(username));
		model.addAttribute("isFavorite", user.getFavorites().stream().anyMatch(e -> e.getId().equals(eventId)));
	}

	@Override
	public void setSpeakersForEvent(Event event, Long speaker1Id, Long speaker2Id, Long speaker3Id) {
		List<Speaker> speakers = new ArrayList<>();

		if (speaker1Id != null && speaker1Id != -1) {
			Speaker s1 = speakerRepository.findById(speaker1Id).orElse(null);
			if (s1 != null) {
				speakers.add(s1);
			}
		}

		if (speaker2Id != null && speaker2Id != -1 && !speaker2Id.equals(speaker1Id)) {
			Speaker s2 = speakerRepository.findById(speaker2Id).orElse(null);
			if (s2 != null) {
				speakers.add(s2);
			}
		}

		if (speaker3Id != null && speaker3Id != -1 && !speaker3Id.equals(speaker1Id)
				&& !speaker3Id.equals(speaker2Id)) {
			Speaker s3 = speakerRepository.findById(speaker3Id).orElse(null);
			if (s3 != null) {
				speakers.add(s3);
			}
		}

		event.setSpeakers(speakers);
	}

	@Override
	public String handleCalculateAction(Event event, Long roomId, Long speaker1Id, Long speaker2Id, Long speaker3Id,
			Model model) {
		Room room = roomRepository.findById(roomId).orElse(null);
		if (room != null) {
			event.setRoom(room);
		}

		int calculatedCheck = event.getBeamerCode() % 97;
		model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));

		model.addAttribute("speaker1Id", speaker1Id);
		model.addAttribute("speaker2Id", speaker2Id);
		model.addAttribute("speaker3Id", speaker3Id);

		setSpeakersForEvent(event, speaker1Id, speaker2Id, speaker3Id);
		populateEventFormModel(model, event);

		return "events/form";
	}

	@Override
	public String validateAndPrepareEvent(Event event, BindingResult result, Long roomId, Long speaker1Id,
			Long speaker2Id, Long speaker3Id, String beamerCheck, Model model) {

		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));
		event.setRoom(room);

		setSpeakersForEvent(event, speaker1Id, speaker2Id, speaker3Id);

		validateEvent(event, result);

		int calculatedCheck = event.getBeamerCode() % 97;
		if (beamerCheck == null || !beamerCheck.equals(String.format("%02d", calculatedCheck))) {
			model.addAttribute("beamerCheckError", "Invalid checksum. Expected: " + calculatedCheck);
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));
			prepareSpeakerIdAttributes(model, speaker1Id, speaker2Id, speaker3Id);
			populateEventFormModel(model, event);
			return "events/form";
		}

		boolean hasSpeaker = (speaker1Id != null && speaker1Id != -1) || (speaker2Id != null && speaker2Id != -1)
				|| (speaker3Id != null && speaker3Id != -1);

		if (!hasSpeaker) {
			model.addAttribute("speakerError", "At least one speaker must be selected.");
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));
			prepareSpeakerIdAttributes(model, speaker1Id, speaker2Id, speaker3Id);
			populateEventFormModel(model, event);
			return "events/form";
		}

		if (result.hasErrors()) {
			prepareSpeakerIdAttributes(model, speaker1Id, speaker2Id, speaker3Id);
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));
			populateEventFormModel(model, event);
			return "events/form";
		}

		return null;
	}

	@Override
	public String validateAndPrepareEventForUpdate(Long id, Event event, BindingResult result, Long roomId,
			Long speaker1Id, Long speaker2Id, Long speaker3Id, String beamerCheck, Model model) {

		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new IllegalArgumentException("Room not found with id:" + roomId));
		event.setRoom(room);

		event.setId(id);

		validateEvent(event, result);

		int calculatedCheck = event.getBeamerCode() % 97;
		if (beamerCheck == null || !beamerCheck.equals(String.format("%02d", calculatedCheck))) {
			model.addAttribute("beamerCheckError", "Invalid checksum. Expected: " + calculatedCheck);
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));
			prepareSpeakerIdAttributes(model, speaker1Id, speaker2Id, speaker3Id);
			populateEventFormModel(model, event);
			return "events/form";
		}

		boolean hasSpeaker = (speaker1Id != null && speaker1Id != -1) || (speaker2Id != null && speaker2Id != -1)
				|| (speaker3Id != null && speaker3Id != -1);

		if (!hasSpeaker) {
			model.addAttribute("speakerError", "At least one speaker must be selected.");
			prepareSpeakerIdAttributes(model, speaker1Id, speaker2Id, speaker3Id);
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));
			populateEventFormModel(model, event);
			return "events/form";
		}

		setSpeakersForEvent(event, speaker1Id, speaker2Id, speaker3Id);

		if (result.hasErrors()) {
			populateEventFormModel(model, event);
			model.addAttribute("beamerCheck", beamerCheck);
			prepareSpeakerIdAttributes(model, speaker1Id, speaker2Id, speaker3Id);
			return "events/form";
		}

		return null;
	}

	@Override
	public String handleAddSpeakerAction(Event event, Long speaker1Id, Long speaker2Id, Long speaker3Id,
			String beamerCheck, Model model) {

		if (event.getSpeakers() == null) {
			event.setSpeakers(new ArrayList<>());
		}
		if (event.getSpeakers().size() < 3) {
			event.getSpeakers().add(new Speaker());
		}

		prepareSpeakerIdAttributes(model, speaker1Id, speaker2Id, speaker3Id);
		populateEventFormModel(model, event);
		model.addAttribute("beamerCheck", beamerCheck);

		return "events/form";
	}

	private void prepareSpeakerIdAttributes(Model model, Long speaker1Id, Long speaker2Id, Long speaker3Id) {
		model.addAttribute("speaker1Id", speaker1Id);
		model.addAttribute("speaker2Id", speaker2Id);
		model.addAttribute("speaker3Id", speaker3Id);
	}

	@Override
	public String prepareEditEventForm(Long id, Model model, HttpSession session) {
		Event event = eventRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

		String returnUrl = (String) session.getAttribute("adminEventsUrl");
		if (returnUrl != null) {
			model.addAttribute("returnUrl", returnUrl);
		}

		if (event.getSpeakers() == null) {
			event.setSpeakers(new ArrayList<>());
		}

		int calculatedCheck = event.getBeamerCode() % 97;
		model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));
		model.addAttribute("event", event);
		populateEventFormModel(model, event);

		return "events/form";
	}

	@Override
	public String prepareConfirmDeleteEvent(Long id, Model model, HttpSession session) {
		try {
			Event event = eventRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

			model.addAttribute("event", event);

			String returnUrl = (String) session.getAttribute("adminEventsUrl");
			if (returnUrl != null) {
				model.addAttribute("returnUrl", returnUrl);
			}

			return "events/confirm-delete";
		} catch (Exception e) {
			model.addAttribute("error", "Error retrieving event: " + e.getMessage());
			return "error";
		}
	}

	private void addSpeakerSelectionAttributes(Model model, Event event) {
		List<Speaker> speakers = event.getSpeakers();
		model.addAttribute("speaker1", speakers.size() > 0 ? speakers.get(0) : null);
		model.addAttribute("speaker2", speakers.size() > 1 ? speakers.get(1) : null);
		model.addAttribute("speaker3", speakers.size() > 2 ? speakers.get(2) : null);
	}
}