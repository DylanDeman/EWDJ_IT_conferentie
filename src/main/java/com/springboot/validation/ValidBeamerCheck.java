package com.springboot.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {BeamerCheckValidator.class})
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBeamerCheck {
    String message() default "Invalid beamer check code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 