package service;

import DTO.RoomWithEventCount;
import domain.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    List<Room> getAllRooms();
    Optional<Room> getRoomById(Long id);
    Optional<Room> getRoomByName(String name);
    Room save(Room room);
    void deleteById(Long id);
    boolean existsByName(String name);

    List<RoomWithEventCount> filterRoomsWithEventCount(Integer capacity, String search, String sort);

    Optional<RoomWithEventCount> getRoomWithEventCountById(Long id);
}