package com.springboot.EWDJ_IT_conferentie;

import domain.Event;
import domain.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.RoomService;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RoomControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoomService roomService;
    
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private RoomController roomController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(roomController)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
                
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Mock message");
    }

    @Test
    public void testListRooms_NoFilters() throws Exception {
        Room room1 = new Room();
        room1.setId(1L);
        room1.setName("A101");
        room1.setCapacity(30);

        Room room2 = new Room();
        room2.setId(2L);
        room2.setName("B202");
        room2.setCapacity(25);

        List<Room> rooms = Arrays.asList(room1, room2);
        
        when(roomService.filterRooms(null, null, "name")).thenReturn(rooms);


        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms/list"))
                .andExpect(model().attribute("rooms", rooms))
                .andExpect(model().attribute("sortFilter", "name"));
        
        verify(roomService, times(1)).filterRooms(null, null, "name");
    }

    @Test
    public void testListRooms_WithFilters() throws Exception {

        Room room = new Room();
        room.setId(1L);
        room.setName("A101");
        room.setCapacity(30);

        List<Room> rooms = Arrays.asList(room);
        
        when(roomService.filterRooms(30, "A1", "capacity")).thenReturn(rooms);


        mockMvc.perform(get("/rooms")
                .param("capacity", "30")
                .param("search", "A1")
                .param("sort", "capacity"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms/list"))
                .andExpect(model().attribute("rooms", rooms))
                .andExpect(model().attribute("capacityFilter", 30))
                .andExpect(model().attribute("searchFilter", "A1"))
                .andExpect(model().attribute("sortFilter", "capacity"));
        
        verify(roomService, times(1)).filterRooms(30, "A1", "capacity");
    }

    @Test
    public void testViewRoom_RoomExists() throws Exception {
        
        Room room = new Room();
        room.setId(1L);
        room.setName("A101");
        room.setCapacity(30);
        
        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

        
        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms/detail"))
                .andExpect(model().attribute("room", room));
        
        verify(roomService, times(1)).getRoomById(1L);
    }

    @Test
    public void testViewRoom_RoomNotFound() throws Exception {
        
        when(roomService.getRoomById(999L)).thenReturn(Optional.empty());

        
        mockMvc.perform(get("/rooms/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rooms"));
        
        verify(roomService, times(1)).getRoomById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testShowAddRoomForm_WithAdminRole() throws Exception {
        
        mockMvc.perform(get("/rooms/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms/form"))
                .andExpect(model().attributeExists("room"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testShowAddRoomForm_WithUserRole_ShouldBeForbidden() throws Exception {
        
        mockMvc.perform(get("/rooms/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAddRoom_Success() throws Exception {
        
        Room roomToSave = new Room();
        roomToSave.setName("A101");
        roomToSave.setCapacity(30);
        
        Room savedRoom = new Room();
        savedRoom.setId(1L);
        savedRoom.setName("A101");
        savedRoom.setCapacity(30);
        
        when(roomService.save(any(Room.class))).thenReturn(savedRoom);

        
        mockMvc.perform(post("/rooms/new")
                .param("name", "A101")
                .param("capacity", "30")
                .flashAttr("room", roomToSave))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rooms"))
                .andExpect(flash().attributeExists("message"));
        
        verify(roomService, times(1)).save(any(Room.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAddRoom_ValidationErrors() throws Exception {
        
        mockMvc.perform(post("/rooms/new")
                .param("name", "invalid name") // Invalid format
                .param("capacity", "60")) // Over capacity limit
                .andExpect(status().isOk())
                .andExpect(view().name("rooms/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testConfirmDeleteRoom_RoomExists() throws Exception {
        
        Room room = new Room();
        room.setId(1L);
        room.setName("A101");
        room.setCapacity(30);
        
        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

        
        mockMvc.perform(get("/rooms/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms/confirm-delete"))
                .andExpect(model().attribute("room", room));
        
        verify(roomService, times(1)).getRoomById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testConfirmDeleteRoom_RoomNotFound() throws Exception {

        when(roomService.getRoomById(999L)).thenReturn(Optional.empty());


        mockMvc.perform(get("/rooms/999/confirm"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rooms"));
        
        verify(roomService, times(1)).getRoomById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteRoom_Success() throws Exception {
        
        Room room = new Room();
        room.setId(1L);
        room.setName("A101");
        room.setCapacity(30);
        room.setEvents(Collections.emptySet());
        
        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));
        doNothing().when(roomService).deleteById(1L);

        
        mockMvc.perform(post("/rooms/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rooms"))
                .andExpect(flash().attributeExists("message"));
        
        verify(roomService, times(1)).getRoomById(1L);
        verify(roomService, times(1)).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteRoom_WithEvents_ShouldFail() throws Exception {
        
        Room room = new Room();
        room.setId(1L);
        room.setName("A101");
        room.setCapacity(30);
        
        Event event = new Event();
        event.setId(100L);
        event.setName("Test Event");
        event.setRoom(room);
        
        room.setEvents(new HashSet<>(Arrays.asList(event)));
        
        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

  
        mockMvc.perform(post("/rooms/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rooms"))
                .andExpect(flash().attributeExists("error"));
        
        verify(roomService, times(1)).getRoomById(1L);
        verify(roomService, never()).deleteById(anyLong());
    }
}