package validation;

import domain.Event;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import service.ValidationService;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class EventValidator implements Validator {


	private static final LocalDate CONFERENCE_START_DATE = LocalDate.of(2025, 6, 1);
	private static final LocalDate CONFERENCE_END_DATE = LocalDate.of(2025, 6, 7);
	private static final BigDecimal MIN_PRICE = new BigDecimal("9.99");
	private static final BigDecimal MAX_PRICE = new BigDecimal("99.99");

	private final ValidationService validationService;


	public EventValidator(ValidationService validationService, MessageSource messageSource) {
		this.validationService = validationService;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Event.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Event event = (Event) target;


		validateCommonRules(event, errors);

		if (event.getId() == null) {
			validateNewEvent(event, errors);
		} else {
			validateExistingEvent(event, errors);
		}
	}


	private void validateCommonRules(Event event, Errors errors) {

		if (event.getName() == null || event.getName().isEmpty()) {
			errors.rejectValue("name", "error.name.required", "Event name is required");
		} else if (!Character.isLetter(event.getName().charAt(0))) {
			errors.rejectValue("name", "error.name.letter", "Event name must start with a letter");
		}
		

		if (event.getDateTime() == null) {
			errors.rejectValue("dateTime", "error.dateTime.required", "Event date/time is required");
		} else {
			LocalDate eventDate = event.getDateTime().toLocalDate();
			if (eventDate.isBefore(CONFERENCE_START_DATE) || eventDate.isAfter(CONFERENCE_END_DATE)) {
				errors.rejectValue("dateTime", "error.date.range",
						"Event must be scheduled during the conference (June 1-7, 2025)");
			}
		}
		

		if (event.getPrice() == null) {
			errors.rejectValue("price", "error.price.required", "Event price is required");
		} else if (event.getPrice().compareTo(MIN_PRICE) < 0 || event.getPrice().compareTo(MAX_PRICE) >= 0) {
			errors.rejectValue("price", "error.price.range", "Event price must be between €9.99 and €99.99");
		}

		if (event.getBeamerCheck() != event.getBeamerCode() % 97) {
			errors.rejectValue("beamerCheck", "error.beamerCheck", "Invalid beamer check code");
		}
	}


	private void validateNewEvent(Event event, Errors errors) {
		validateRoomAvailabilityForNewEvent(event, errors);
		validateNameUniquenessForNewEvent(event, errors);
	}

	private void validateExistingEvent(Event event, Errors errors) {
		validateRoomAvailabilityForUpdate(event, errors);
		validateNameUniquenessForUpdate(event, errors);
	}



	private void validateRoomAvailabilityForNewEvent(Event event, Errors errors) {
		if (event.getRoom() == null) {
			errors.rejectValue("room", "error.room.required", "Room is required");
			return;
		}

		if (event.getDateTime() == null) {
			return;
		}

		boolean isRoomAvailable = validationService.isRoomAvailable(event.getDateTime(), event.getRoom().getId());

		if (!isRoomAvailable) {
			errors.rejectValue("room", "error.room.occupied", "The selected room is not available at this time");
		}
	}

	private void validateRoomAvailabilityForUpdate(Event event, Errors errors) {
		if (event.getRoom() == null || event.getDateTime() == null || event.getId() == null) {
			return;
		}

		boolean isRoomAvailable = validationService.isRoomAvailableExcludingEvent(
				event.getDateTime(), event.getRoom().getId(), event.getId());

		if (!isRoomAvailable) {
			errors.rejectValue("room", "error.room.occupied", "The selected room is not available at this time");
		}
	}

	private void validateNameUniquenessForNewEvent(Event event, Errors errors) {
		if (event.getName() == null || event.getName().isEmpty() || event.getDateTime() == null) {
			return;
		}

		boolean isNameUnique = validationService.isEventNameUniqueOnDate(event.getDateTime(), event.getName());

		if (!isNameUnique) {
			errors.rejectValue("name", "error.name.exists", "An event with this name already exists on the same date");
		}
	}

	private void validateNameUniquenessForUpdate(Event event, Errors errors) {
		if (event.getName() == null || event.getName().isEmpty() || event.getDateTime() == null
				|| event.getId() == null) {
			return;
		}

		boolean nameExists = validationService.existsByNameAndDateExcludingId(
				event.getName(), event.getDateTime().toLocalDate(), event.getId());

		if (nameExists) {
			errors.rejectValue("name", "error.name.exists",
					"An event with this name already exists on the same date");
		}
	}
}