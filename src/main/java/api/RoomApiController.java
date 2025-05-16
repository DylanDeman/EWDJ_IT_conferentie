package api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import domain.Event;
import domain.Room;
import jakarta.validation.Valid;
import service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomApiController {

	private final RoomService roomService;

	public RoomApiController(RoomService roomService) {
		this.roomService = roomService;
	}

	@GetMapping
	public ResponseEntity<List<Room>> getAllRooms() {
		return ResponseEntity.ok(roomService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Room> getRoom(@PathVariable Long id) {
		return roomService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Room> createRoom(@Valid @RequestBody Room room) {
		Room savedRoom = roomService.save(room);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedRoom.getId())
				.toUri();
		return ResponseEntity.created(location).body(savedRoom);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Room> updateRoom(@PathVariable Long id, @Valid @RequestBody Room room) {
		return roomService.findById(id).map(existingRoom -> {
			room.setId(id);
			return ResponseEntity.ok(roomService.save(room));
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
		return roomService.findById(id).map(room -> {
			roomService.deleteById(id);
			return ResponseEntity.noContent().<Void>build();
		}).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}/events")
	public ResponseEntity<List<Event>> getRoomEvents(@PathVariable Long id) {
		return roomService.findById(id).map(room -> ResponseEntity.ok(roomService.getRoomEvents(id)))
				.orElse(ResponseEntity.notFound().build());
	}
}