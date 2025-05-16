package com.springboot.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.repository.RoomRepository;
import com.springboot.service.RoomService;

import domain.Event;
import domain.Room;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;

	public RoomServiceImpl(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Room> findAll() {
		return roomRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Room> findById(Long id) {
		return roomRepository.findById(id);
	}

	@Override
	public Room save(Room room) {
		return roomRepository.save(room);
	}

	@Override
	public void deleteById(Long id) {
		roomRepository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Event> getRoomEvents(Long roomId) {
		return roomRepository.findEventsByRoomId(roomId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Room> getAllRooms() {
		return roomRepository.findAll();
	}

	@Override
	public Room createRoom(Room room) {
		validateRoom(room);
		return roomRepository.save(room);
	}

	@Override
	public Room updateRoom(Long id, Room room) {
		Room existingRoom = roomRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Room not found"));

		validateRoom(room);
		room.setId(id);
		return roomRepository.save(room);
	}

	@Override
	public void deleteRoom(Long id) {
		roomRepository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByName(String name) {
		return roomRepository.existsByName(name);
	}

	@Override
	@Transactional(readOnly = true)
	public int getRoomCapacity(Long roomId) {
		return roomRepository.findById(roomId).map(Room::getCapacity)
				.orElseThrow(() -> new EntityNotFoundException("Room not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Room> getRoomById(Long id) {
		return roomRepository.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Room> getRoomByName(String name) {
		return roomRepository.findByName(name);
	}

	private void validateRoom(Room room) {
		if (room.getCapacity() < 0 || room.getCapacity() > 50) {
			throw new IllegalArgumentException("Room capacity must be between 0 and 50");
		}

		if (!room.getName().matches("^[A-Z]\\d{3}$")) {
			throw new IllegalArgumentException("Room name must start with a letter followed by 3 digits");
		}

		if (existsByName(room.getName())) {
			throw new IllegalArgumentException("Room name must be unique");
		}
	}
}