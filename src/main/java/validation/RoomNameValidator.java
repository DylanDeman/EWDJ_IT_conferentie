package validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;

public class RoomNameValidator implements ConstraintValidator<ValidRoomName, String> {
    
    @Autowired
    private RoomService roomService;
    

    @Override
    public boolean isValid(String roomName, ConstraintValidatorContext context) {
        if (roomName == null || roomName.isEmpty()) {
            return false;
        }
        

        boolean formatValid = roomName.matches("^[A-Z]\\d{3}$");
        
        if (!formatValid) {
            return false;
        }
        

        if (roomService != null && roomService.existsByName(roomName)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Room name must be unique")
                  .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}