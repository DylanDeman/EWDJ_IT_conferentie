package com.springboot.EWDJ_IT_conferentie;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import domain.Room;
import service.RoomService;

public class RoomRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomRestController roomRestController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(roomRestController).build();
    }

    @Test
    public void testGetAllRooms() throws Exception {
        Room room1 = new Room();
        room1.setId(1L);
        room1.setName("A101");
        room1.setCapacity(30);

        Room room2 = new Room();
        room2.setId(2L);
        room2.setName("B202");
        room2.setCapacity(25);

        List<Room> rooms = Arrays.asList(room1, room2);
        
        when(roomService.getAllRooms()).thenReturn(rooms);

        mockMvc.perform(get("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("A101"))
                .andExpect(jsonPath("$[0].capacity").value(30))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("B202"))
                .andExpect(jsonPath("$[1].capacity").value(25));
        
        verify(roomService, times(1)).getAllRooms();
    }

    @ParameterizedTest
    @MethodSource("roomCapacityTestCases")
    public void testGetRoomCapacity(String roomName, Optional<Room> returnedRoom, int expectedStatus, String expectedContent) throws Exception {
        when(roomService.getRoomByName(roomName)).thenReturn(returnedRoom);

        mockMvc.perform(get("/api/rooms/{name}/capacity", roomName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus))
                .andExpect(expectedContent == null ? status().isNotFound() : content().string(expectedContent));
        
        verify(roomService, times(1)).getRoomByName(roomName);
    }
    
    private static Stream<Arguments> roomCapacityTestCases() {
        Room existingRoom = new Room();
        existingRoom.setId(1L);
        existingRoom.setName("A101");
        existingRoom.setCapacity(30);
        
        return Stream.of(
            Arguments.of("A101", Optional.of(existingRoom), 200, "30"),
            Arguments.of("Z999", Optional.empty(), 404, null)
        );
    }
}