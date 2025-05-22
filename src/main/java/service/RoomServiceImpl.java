package service;

import domain.Room;
import DTO.RoomWithEventCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.RoomRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
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
    public boolean existsByName(String name) {
        return roomRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomWithEventCount> filterRoomsWithEventCount(Integer capacity, String search, String sort) {

        List<Object[]> results = roomRepository.findAllRoomsWithEventCounts();

        List<RoomWithEventCount> rooms = results.stream()
                .map(RoomWithEventCount::new)
                .collect(Collectors.toList());


        if (capacity != null && capacity > 0) {
            rooms = rooms.stream()
                    .filter(room -> room.getCapacity() >= capacity)
                    .collect(Collectors.toList());

        }

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            rooms = rooms.stream()
                    .filter(room -> room.getName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());

        }

        if ("capacity".equals(sort)) {
            rooms = rooms.stream()
                    .sorted(Comparator.comparing(RoomWithEventCount::getCapacity).reversed())
                    .collect(Collectors.toList());
        } else {
            rooms = rooms.stream()
                    .sorted(Comparator.comparing(room -> room.getName().toLowerCase()))
                    .collect(Collectors.toList());
        }

        return rooms;
    }

    public Optional<RoomWithEventCount> getRoomWithEventCountById(Long id) {
        Optional<Room> roomOpt = roomRepository.findById(id);

        if (roomOpt.isEmpty()) {
            return Optional.empty();
        }

        Room room = roomOpt.get();
        Long eventCount = room.getEvents() != null ? (long) room.getEvents().size() : 0L;


        RoomWithEventCount roomWithCount = new RoomWithEventCount(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                eventCount
        );

        return Optional.of(roomWithCount);
    }
}