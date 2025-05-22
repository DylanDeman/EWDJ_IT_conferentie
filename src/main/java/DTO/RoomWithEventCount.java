package DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoomWithEventCount {
    private Long id;
    private String name;
    private Integer capacity;
    private Long eventCount;


    public RoomWithEventCount(Object[] row) {
        this.id = (Long) row[0];
        this.name = (String) row[1];
        this.capacity = (Integer) row[2];
        this.eventCount = (Long) row[3];
    }


    public RoomWithEventCount(Long id, String name, Integer capacity, Long eventCount) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.eventCount = eventCount;
    }


    public int getEventCount() {
        return eventCount != null ? eventCount.intValue() : 0;
    }


    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setEventCount(Long eventCount) { this.eventCount = eventCount; }
}