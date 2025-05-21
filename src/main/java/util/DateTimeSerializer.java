package util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static util.DateFormat.DATE_FORMATTER;

public class DateTimeSerializer extends JsonSerializer<LocalDateTime> {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        if (value != null) {
            gen.writeString(value.format(DATE_TIME_FORMATTER));
        } else {
            gen.writeNull();
        }
    }
}