package com.springboot.EWDJ_IT_conferentie;

import domain.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.RoomService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {

    @Autowired
    private RoomService roomService;
    
    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{name}/capacity")
    public ResponseEntity<Integer> getRoomCapacity(@PathVariable String name) {
        Optional<Room> room = roomService.getRoomByName(name);
        return room.map(r -> ResponseEntity.ok(r.getCapacity()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}