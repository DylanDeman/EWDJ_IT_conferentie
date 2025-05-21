package repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import domain.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	@Query("SELECT e FROM Event e WHERE DATE(e.dateTime) = :date ORDER BY e.dateTime, e.name")
	List<Event> findByDateOrderByDateTimeAndName(LocalDate date);

	boolean existsByRoomIdAndDateTimeEquals(Long roomId, LocalDateTime dateTime);

}
