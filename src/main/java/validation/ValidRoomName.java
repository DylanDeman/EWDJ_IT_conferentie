package validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RoomNameValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoomName {
    String message() default "Room name must start with a capital letter followed by 3 digits";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}