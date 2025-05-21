package validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RoomCapacityValidator implements ConstraintValidator<ValidRoomCapacity, Integer> {
    
    private int min;
    private int max;
    
    @Override
    public void initialize(ValidRoomCapacity constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }
    
    @Override
    public boolean isValid(Integer capacity, ConstraintValidatorContext context) {
        if (capacity == null) {
            return false;
        }
        
        return capacity >= min && capacity <= max;
    }
}