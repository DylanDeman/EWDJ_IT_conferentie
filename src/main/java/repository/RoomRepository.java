package repository;

import domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByName(String name);
    Optional<Room> findByName(String name);

    @Query("SELECT r.id, r.name, r.capacity, COUNT(e.id) " +
            "FROM Room r LEFT JOIN Event e ON r.id = e.room.id " +
            "GROUP BY r.id, r.name, r.capacity " +
            "ORDER BY r.name")
    List<Object[]> findAllRoomsWithEventCounts();
}