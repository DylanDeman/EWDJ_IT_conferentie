package validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import domain.Event;
import service.EventService;

@Component
public class EventValidator implements Validator {

	private static final LocalDate CONFERENCE_START_DATE = LocalDate.of(2025, 5, 1);
	private static final LocalDate CONFERENCE_END_DATE = LocalDate.of(2025, 5, 3);
	private static final BigDecimal MIN_PRICE = new BigDecimal("9.99");
	private static final BigDecimal MAX_PRICE = new BigDecimal("99.99");

	@Autowired
	private EventService eventService;

	@Autowired
	private MessageSource messageSource;

	@Override
	public boolean supports(Class<?> clazz) {
		return Event.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Event event = (Event) target;

		if (event.getName() != null && !event.getName().isEmpty() && !Character.isLetter(event.getName().charAt(0))) {
			errors.rejectValue("name", "error.name.letter");
		}

		if (event.getDateTime() != null) {
			LocalDate eventDate = event.getDateTime().toLocalDate();
			if (eventDate.isBefore(CONFERENCE_START_DATE) || eventDate.isAfter(CONFERENCE_END_DATE)) {
				errors.rejectValue("dateTime", "error.date.range");
			}

			// Note: Name uniqueness check moved to the controller for different behavior
			// between create/edit

			if (event.getRoom() != null) {
				boolean roomBusy = eventService.isRoomAvailable(event.getDateTime(), event.getRoom().getId());
				if (roomBusy) {
					errors.rejectValue("room", "error.room.occupied");
				}
			}
		}

		// Validate price range
		if (event.getPrice() != null) {
			if (event.getPrice().compareTo(MIN_PRICE) < 0 || event.getPrice().compareTo(MAX_PRICE) >= 0) {
				errors.rejectValue("price", "error.price.range");
			}
		}
	}

	/**
	 * Validate beamer code and checksum
	 * 
	 * @param beamerCode  The 4-digit beamer code
	 * @param beamerCheck The 2-digit checksum
	 * @return Validation error message or null if valid
	 */
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
			return null; // Valid
		} catch (NumberFormatException e) {
			return messageSource.getMessage("error.beamer.invalid", null, LocaleContextHolder.getLocale());
		}
	}

	/**
	 * Validate that at least one speaker is selected and no duplicates
	 * 
	 * @param speaker1Id First speaker ID
	 * @param speaker2Id Second speaker ID
	 * @param speaker3Id Third speaker ID
	 * @return Validation error message or null if valid
	 */
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

		return null; // Valid
	}
}