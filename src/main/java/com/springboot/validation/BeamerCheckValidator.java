package com.springboot.validation;

import com.springboot.domain.Event;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BeamerCheckValidator implements ConstraintValidator<ValidBeamerCheck, Event> {
    
    @Override
    public void initialize(ValidBeamerCheck constraintAnnotation) {
    }

    @Override
    public boolean isValid(Event event, ConstraintValidatorContext context) {
        if (event == null) {
            return true;
        }
        return event.validateBeamerCheck();
    }
} 