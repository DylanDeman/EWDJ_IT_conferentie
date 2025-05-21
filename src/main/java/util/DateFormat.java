package util;

import java.time.format.DateTimeFormatter;

public interface DateFormat {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

}