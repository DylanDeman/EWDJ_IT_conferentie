package repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import domain.Event;
import domain.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
	Optional<Room> findByName(String name);

	boolean existsByName(String name);

	@Query("SELECT e FROM Event e WHERE e.room.id = :roomId")
	List<Event> findEventsByRoomId(@Param("roomId") Long roomId);
}