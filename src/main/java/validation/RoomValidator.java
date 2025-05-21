package validation;

import domain.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import service.RoomService;

@Component
public class RoomValidator implements Validator {

    @Autowired
    private RoomService roomService;
    
    @Override
    public boolean supports(Class<?> clazz) {
        return Room.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Room room = (Room) target;

        // Check name format
        if (room.getName() != null && !room.getName().matches("^[A-Z]\\d{3}$")) {
            errors.rejectValue("name", "room.name.format", 
                             "Room name must start with a capital letter followed by 3 digits");
        }


        if (room.getCapacity() < 0 || room.getCapacity() > 50) {
            errors.rejectValue("capacity", "room.capacity.range", 
                             "Room capacity must be between 0 and 50");
        }

        if (room.getId() == null && 
            room.getName() != null && 
            roomService.existsByName(room.getName())) {
            errors.rejectValue("name", "room.name.unique", 
                             "Room name must be unique");
        }
    }
}