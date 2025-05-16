package validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ConferencePeriodValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConferencePeriod {
    String message() default "{event.datetime.conference.period}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 