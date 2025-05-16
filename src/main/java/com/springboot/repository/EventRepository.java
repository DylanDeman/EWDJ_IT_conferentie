package com.springboot.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import domain.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	@Query("SELECT e FROM Event e WHERE CAST(e.dateTime as date) = CAST(:date as date)")
	List<Event> findByDate(@Param("date") LocalDateTime date);

	@Query("SELECT e FROM Event e WHERE e.dateTime = :date ORDER BY e.dateTime, e.name")
	List<Event> findByDateOrderByDateTimeAndName(@Param("date") LocalDateTime date);

	@Query("SELECT e FROM Event e WHERE e.room.id = :roomId")
	List<Event> findEventsByRoomId(@Param("roomId") Long roomId);

	@Query("SELECT e FROM Event e JOIN e.favoritedBy u WHERE u.username = :username")
	List<Event> findUserFavorites(@Param("username") String username);

	@Query("SELECT COUNT(e) FROM Event e JOIN e.favoritedBy u WHERE u.username = :username")
	long countUserFavorites(@Param("username") String username);

	@Query("SELECT e.room.capacity FROM Event e WHERE e.room.id = :roomId")
	Optional<Integer> findRoomCapacity(@Param("roomId") Long roomId);

	@Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.room.id = :roomId AND e.dateTime = :dateTime")
	boolean existsByRoomIdAndDateTime(@Param("roomId") Long roomId, @Param("dateTime") LocalDateTime dateTime);

	@Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.name = :name AND e.dateTime = :date")
	boolean existsByNameAndDateTime(@Param("name") String name, @Param("date") LocalDateTime date);

	List<Event> findAllByOrderByDateTimeAsc();
}