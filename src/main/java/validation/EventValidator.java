package validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import domain.Event;
import repository.EventRepository;

@Component
public class EventValidator implements Validator {

	// Updated conference dates to first week of June
	private static final LocalDate CONFERENCE_START_DATE = LocalDate.of(2025, 6, 1);
	private static final LocalDate CONFERENCE_END_DATE = LocalDate.of(2025, 6, 7);
	private static final BigDecimal MIN_PRICE = new BigDecimal("9.99");
	private static final BigDecimal MAX_PRICE = new BigDecimal("99.99");

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private MessageSource messageSource;

	@Override
	public boolean supports(Class<?> clazz) {
		return Event.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Event event = (Event) target;

		validateName(event, errors);
		validateDateTime(event, errors);
		validatePrice(event, errors);
		validateBeamerCheckCode(event, errors);

		if (event.getId() == null) {
			validateRoomAvailabilityForNewEvent(event, errors);
			validateNameUniquenessForNewEvent(event, errors);
		} else {
			validateRoomAvailabilityForUpdate(event, errors);
			validateNameUniquenessForUpdate(event, errors);
		}
	}

	private void validateName(Event event, Errors errors) {
		if (event.getName() == null || event.getName().isEmpty()) {
			errors.rejectValue("name", "error.name.required", "Event name is required");
			return;
		}

		if (!Character.isLetter(event.getName().charAt(0))) {
			errors.rejectValue("name", "error.name.letter", "Event name must start with a letter");
		}
	}

	private void validateDateTime(Event event, Errors errors) {
		if (event.getDateTime() == null) {
			errors.rejectValue("dateTime", "error.dateTime.required", "Event date/time is required");
			return;
		}

		LocalDate eventDate = event.getDateTime().toLocalDate();
		if (eventDate.isBefore(CONFERENCE_START_DATE) || eventDate.isAfter(CONFERENCE_END_DATE)) {
			errors.rejectValue("dateTime", "error.date.range",
					"Event must be scheduled during the conference (June 1-7, 2025)");
		}
	}

	private void validatePrice(Event event, Errors errors) {
		if (event.getPrice() == null) {
			errors.rejectValue("price", "error.price.required", "Event price is required");
			return;
		}

		if (event.getPrice().compareTo(MIN_PRICE) < 0 || event.getPrice().compareTo(MAX_PRICE) >= 0) {
			errors.rejectValue("price", "error.price.range", "Event price must be between €9.99 and €99.99");
		}
	}

	private void validateBeamerCheckCode(Event event, Errors errors) {
		if (event.getBeamerCheck() != event.getBeamerCode() % 97) {
			errors.rejectValue("beamerCheck", "error.beamerCheck", "Invalid beamer check code");
		}
	}

	private void validateRoomAvailabilityForNewEvent(Event event, Errors errors) {
		if (event.getRoom() == null) {
			errors.rejectValue("room", "error.room.required", "Room is required");
			return;
		}

		if (event.getDateTime() == null) {
			return;
		}

		boolean isRoomAvailable = !eventRepository.existsByRoomIdAndDateTime(event.getRoom().getId(),
				event.getDateTime());

		if (!isRoomAvailable) {
			errors.rejectValue("room", "error.room.occupied", "The selected room is not available at this time");
		}
	}

	private void validateRoomAvailabilityForUpdate(Event event, Errors errors) {
		if (event.getRoom() == null || event.getDateTime() == null || event.getId() == null) {
			return;
		}

		Event existingEvent = eventRepository.findById(event.getId()).orElse(null);
		if (existingEvent == null) {
			errors.reject("error.event.notFound", "Event not found");
			return;
		}

		boolean timeOrRoomChanged = !existingEvent.getDateTime().equals(event.getDateTime())
				|| existingEvent.getRoom().getId() != event.getRoom().getId();

		if (timeOrRoomChanged) {
			List<Event> conflictingEvents = eventRepository.findAll().stream()
					.filter(e -> e.getDateTime().equals(event.getDateTime())
							&& e.getRoom().getId() == event.getRoom().getId() && !e.getId().equals(event.getId()))
					.collect(Collectors.toList());

			if (!conflictingEvents.isEmpty()) {
				errors.rejectValue("room", "error.room.occupied", "The selected room is not available at this time");
			}
		}
	}

	private void validateNameUniquenessForNewEvent(Event event, Errors errors) {
		if (event.getName() == null || event.getName().isEmpty() || event.getDateTime() == null) {
			return;
		}

		LocalDate eventDate = event.getDateTime().toLocalDate();

		List<Event> eventsWithSameName = eventRepository.findAll().stream().filter(e -> e.getDateTime() != null
				&& e.getDateTime().toLocalDate().equals(eventDate) && e.getName().equalsIgnoreCase(event.getName()))
				.collect(Collectors.toList());

		if (!eventsWithSameName.isEmpty()) {
			errors.rejectValue("name", "error.name.exists", "An event with this name already exists on the same date");
		}
	}

	private void validateNameUniquenessForUpdate(Event event, Errors errors) {
		if (event.getName() == null || event.getName().isEmpty() || event.getDateTime() == null
				|| event.getId() == null) {
			return;
		}

		Event existingEvent = eventRepository.findById(event.getId()).orElse(null);
		if (existingEvent == null) {
			errors.reject("error.event.notFound", "Event not found");
			return;
		}

		boolean nameOrDateChanged = !existingEvent.getName().equalsIgnoreCase(event.getName())
				|| !existingEvent.getDateTime().toLocalDate().equals(event.getDateTime().toLocalDate());

		if (nameOrDateChanged) {
			LocalDate eventDate = event.getDateTime().toLocalDate();

			List<Event> eventsWithSameName = eventRepository.findAll().stream()
					.filter(e -> e.getDateTime() != null && e.getDateTime().toLocalDate().equals(eventDate)
							&& e.getName().equalsIgnoreCase(event.getName()) && !e.getId().equals(event.getId()))
					.collect(Collectors.toList());

			if (!eventsWithSameName.isEmpty()) {
				errors.rejectValue("name", "error.name.exists",
						"An event with this name already exists on the same date");
			}
		}
	}

	public String validateBeamerChecksum(int beamerCode, String beamerCheckStr) {
		if (beamerCheckStr == null || beamerCheckStr.trim().isEmpty()) {
			return messageSource.getMessage("error.beamer.required", null, LocaleContextHolder.getLocale());
		}

		try {
			int check = Integer.parseInt(beamerCheckStr);
			int expectedCheck = beamerCode % 97;

			if (check != expectedCheck) {
				return messageSource.getMessage("error.beamer.checksum", new Object[] { expectedCheck },
						LocaleContextHolder.getLocale());
			}
			return null;
		} catch (NumberFormatException e) {
			return messageSource.getMessage("error.beamer.invalid", null, LocaleContextHolder.getLocale());
		}
	}

	public String validateSpeakers(Long speaker1Id, Long speaker2Id, Long speaker3Id) {
		boolean hasSpeaker = (speaker1Id != null && speaker1Id != -1) || (speaker2Id != null && speaker2Id != -1)
				|| (speaker3Id != null && speaker3Id != -1);

		if (!hasSpeaker) {
			return messageSource.getMessage("error.speaker.required", null, LocaleContextHolder.getLocale());
		}

		Set<Long> speakerSet = new HashSet<>();
		if (speaker1Id != null && speaker1Id != -1) {
			speakerSet.add(speaker1Id);
		}
		if (speaker2Id != null && speaker2Id != -1) {
			if (!speakerSet.add(speaker2Id)) {
				return messageSource.getMessage("error.speaker.duplicate", null, LocaleContextHolder.getLocale());
			}
		}
		if (speaker3Id != null && speaker3Id != -1) {
			if (!speakerSet.add(speaker3Id)) {
				return messageSource.getMessage("error.speaker.duplicate", null, LocaleContextHolder.getLocale());
			}
		}

		return null;
	}

	public boolean isRoomAvailable(LocalDateTime dateTime, Long roomId) {
		return !eventRepository.existsByRoomIdAndDateTime(roomId, dateTime);
	}

	public boolean isEventNameUniqueOnDate(LocalDateTime date, String name) {
		if (date == null || name == null || name.isEmpty()) {
			return true;
		}

		LocalDate eventDate = date.toLocalDate();

		List<Event> sameNameEvents = eventRepository
				.findAll().stream().filter(e -> e.getDateTime() != null
						&& e.getDateTime().toLocalDate().equals(eventDate) && e.getName().equalsIgnoreCase(name))
				.collect(Collectors.toList());

		return sameNameEvents.isEmpty();
	}

	public boolean existsByNameAndDate(String name, LocalDate date) {
		if (name == null || date == null) {
			return false;
		}

		return eventRepository.findAll().stream().anyMatch(e -> e.getName().equalsIgnoreCase(name)
				&& e.getDateTime() != null && e.getDateTime().toLocalDate().equals(date));
	}

	public boolean existsByNameAndDateExcludingId(String name, LocalDate date, Long eventId) {
		if (name == null || date == null || eventId == null) {
			return false;
		}

		List<Event> eventsOnDate = eventRepository.findAll().stream()
				.filter(e -> e.getDateTime() != null && e.getDateTime().toLocalDate().equals(date))
				.collect(Collectors.toList());

		return eventsOnDate.stream().filter(e -> !e.getId().equals(eventId))
				.anyMatch(e -> e.getName().equalsIgnoreCase(name));
	}
}