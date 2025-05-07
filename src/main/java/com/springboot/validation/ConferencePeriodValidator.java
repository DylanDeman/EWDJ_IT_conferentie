package com.springboot.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class ConferencePeriodValidator implements ConstraintValidator<ConferencePeriod, LocalDateTime> {

    private static final LocalDateTime CONFERENCE_START = LocalDateTime.of(2025, 5, 1, 9, 0);
    private static final LocalDateTime CONFERENCE_END = LocalDateTime.of(2025, 5, 3, 17, 0);

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle this
        }
        return !value.isBefore(CONFERENCE_START) && !value.isAfter(CONFERENCE_END);
    }
} 