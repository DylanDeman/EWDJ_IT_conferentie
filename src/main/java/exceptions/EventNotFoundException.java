package exceptions;

public class EventNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;


    public EventNotFoundException(String fieldName, Object fieldValue) {
        super(String.format("Event not found with %s : '%s'", fieldName, fieldValue));
    }
}