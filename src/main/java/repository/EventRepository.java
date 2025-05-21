package repository;

import domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	@Query("SELECT e FROM Event e WHERE DATE(e.dateTime) = :date ORDER BY e.dateTime, e.name")
	List<Event> findByDateOrderByDateTimeAndName(LocalDate date);

	boolean existsByRoomIdAndDateTimeEquals(Long roomId, LocalDateTime dateTime);

}
