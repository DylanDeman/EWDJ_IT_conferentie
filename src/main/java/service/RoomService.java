package service;

import java.util.List;
import java.util.Optional;

import domain.Event;
import domain.Room;

public interface RoomService {
	List<Room> findAll();

	Optional<Room> findById(Long id);

	Room save(Room room);

	void deleteById(Long id);

	List<Event> getRoomEvents(Long roomId);

	List<Room> getAllRooms();

	Room createRoom(Room room);

	Room updateRoom(Long id, Room room);

	void deleteRoom(Long id);

	boolean existsByName(String name);

	int getRoomCapacity(Long roomId);

	Optional<Room> getRoomById(Long id);

	Optional<Room> getRoomByName(String name);
}