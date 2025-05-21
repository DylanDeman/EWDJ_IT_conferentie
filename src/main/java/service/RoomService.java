package service;

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
    List<Room> filterRooms(Integer capacity, String search, String sort);
}