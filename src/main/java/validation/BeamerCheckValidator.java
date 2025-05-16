package validation;

import domain.Event;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BeamerCheckValidator implements ConstraintValidator<ValidBeamerCheck, Event> {

	@Override
	public boolean isValid(Event event, ConstraintValidatorContext context) {
		if (event == null)
			return true; // Let @NotNull handle nulls

		int beamerCode = event.getBeamerCode();
		int beamerCheck = event.getBeamerCheck();

		return beamerCheck == (beamerCode % 97);
	}
}
