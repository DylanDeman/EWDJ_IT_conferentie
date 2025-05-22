package com.springboot.EWDJ_IT_conferentie;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import DTO.RoomWithEventCount;
import domain.Event;
import domain.Room;
import service.RoomService;

public class RoomControllerTest {

    private RoomService roomService;
    private MessageSource messageSource;
    private RoomController roomController;
    private Model model;
    private RedirectAttributes redirectAttributes;
    private BindingResult bindingResult;

    @BeforeEach
    public void setup() {
        roomService = mock(RoomService.class);
        messageSource = mock(MessageSource.class);
        model = mock(Model.class);
        redirectAttributes = mock(RedirectAttributes.class);
        bindingResult = mock(BindingResult.class);

        roomController = new RoomController();
        ReflectionTestUtils.setField(roomController, "roomService", roomService);
        ReflectionTestUtils.setField(roomController, "messageSource", messageSource);
    }

    @Test
    public void testListRooms_NoFilters() {
        List<RoomWithEventCount> rooms = Arrays.asList(
                createRoomWithEventCount(1L, "Room 1", 50, 2),
                createRoomWithEventCount(2L, "Room 2", 100, 0)
        );
        when(roomService.filterRoomsWithEventCount(null, null, "name")).thenReturn(rooms);

        String viewName = roomController.listRooms(model, null, null, "name");

        verify(roomService).filterRoomsWithEventCount(null, null, "name");
        verify(model).addAttribute("rooms", rooms);
        verify(model).addAttribute("sortFilter", "name");
        assert viewName.equals("rooms/list");
    }

    @Test
    public void testListRooms_WithFilters() {
        List<RoomWithEventCount> rooms = Arrays.asList(
                createRoomWithEventCount(1L, "Conference Room", 50, 1),
                createRoomWithEventCount(2L, "Conference Hall", 200, 3)
        );
        when(roomService.filterRoomsWithEventCount(50, "conference", "capacity")).thenReturn(rooms);

        String viewName = roomController.listRooms(model, 50, "conference", "capacity");

        verify(roomService).filterRoomsWithEventCount(50, "conference", "capacity");
        verify(model).addAttribute("rooms", rooms);
        verify(model).addAttribute("capacityFilter", 50);
        verify(model).addAttribute("searchFilter", "conference");
        verify(model).addAttribute("sortFilter", "capacity");
        assert viewName.equals("rooms/list");
    }

    @Test
    public void testViewRoom_ExistingRoom() {
        Room room = createRoom(1L, "Hall A", 100);
        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

        String viewName = roomController.viewRoom(1L, model);

        verify(roomService).getRoomById(1L);
        verify(model).addAttribute("room", room);
        assert viewName.equals("rooms/detail");
    }

    @Test
    public void testViewRoom_NonExistentRoom() {
        when(roomService.getRoomById(999L)).thenReturn(Optional.empty());

        String viewName = roomController.viewRoom(999L, model);

        verify(roomService).getRoomById(999L);
        assert viewName.equals("redirect:/rooms");
    }

    @Test
    public void testShowAddRoomForm() {
        String viewName = roomController.showAddRoomForm(model);

        verify(model).addAttribute(eq("room"), any(Room.class));
        assert viewName.equals("rooms/form");
    }

    @Test
    public void testAddRoom_Success() {
        Room room = createRoom(null, "New Room", 200);
        Room savedRoom = createRoom(1L, "New Room", 200);
        Locale locale = Locale.ENGLISH;

        when(bindingResult.hasErrors()).thenReturn(false);
        when(roomService.save(room)).thenReturn(savedRoom);

        try (MockedStatic<LocaleContextHolder> localeHolder = mockStatic(LocaleContextHolder.class)) {
            localeHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            when(messageSource.getMessage(eq("room.added"), any(), eq(locale))).thenReturn("Room added");

            String viewName = roomController.addRoom(room, bindingResult, redirectAttributes);

            verify(roomService).save(room);
            verify(redirectAttributes).addFlashAttribute("message", "Room added");
            assert viewName.equals("redirect:/rooms");
        }
    }

    @Test
    public void testAddRoom_ValidationError() {
        Room room = createRoom(null, "", 0);

        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = roomController.addRoom(room, bindingResult, redirectAttributes);

        assert viewName.equals("rooms/form");
    }

    @Test
    public void testAddRoom_ServiceException() {
        Room room = createRoom(null, "Duplicate", 100);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(roomService.save(room)).thenThrow(new RuntimeException("Room with this name already exists"));

        String viewName = roomController.addRoom(room, bindingResult, redirectAttributes);

        verify(bindingResult).rejectValue("name", "error.room", "Room with this name already exists");
        assert viewName.equals("rooms/form");
    }

    @Test
    public void testConfirmDeleteRoom_ExistingRoom() {
        // Updated test to use RoomWithEventCount
        List<RoomWithEventCount> rooms = Arrays.asList(
                createRoomWithEventCount(1L, "Room to Delete", 50, 0),
                createRoomWithEventCount(2L, "Other Room", 100, 2)
        );
        
        when(roomService.filterRoomsWithEventCount(null, null, "name")).thenReturn(rooms);

        String viewName = roomController.confirmDeleteRoom(1L, model, 100, "test", "name");

        verify(roomService).filterRoomsWithEventCount(null, null, "name");
        verify(model).addAttribute("room", rooms.get(0));
        verify(model).addAttribute("canDelete", true);
        verify(model).addAttribute("capacityFilter", 100);
        verify(model).addAttribute("searchFilter", "test");
        verify(model).addAttribute("sortFilter", "name");
        assert viewName.equals("rooms/confirm-delete");
    }

    @Test
    public void testConfirmDeleteRoom_RoomWithEvents() {
        // Room with events
        List<RoomWithEventCount> rooms = Arrays.asList(
                createRoomWithEventCount(1L, "Room with Events", 50, 3),
                createRoomWithEventCount(2L, "Other Room", 100, 2)
        );
        
        when(roomService.filterRoomsWithEventCount(null, null, "name")).thenReturn(rooms);

        String viewName = roomController.confirmDeleteRoom(1L, model, 100, "test", "name");

        verify(roomService).filterRoomsWithEventCount(null, null, "name");
        verify(model).addAttribute("room", rooms.get(0));
        verify(model).addAttribute("canDelete", false);
        verify(model).addAttribute("capacityFilter", 100);
        verify(model).addAttribute("searchFilter", "test");
        verify(model).addAttribute("sortFilter", "name");
        assert viewName.equals("rooms/confirm-delete");
    }

    @Test
    public void testConfirmDeleteRoom_NonExistentRoom() {
        // Empty list simulating room not found
        List<RoomWithEventCount> rooms = Arrays.asList(
                createRoomWithEventCount(2L, "Other Room", 100, 2)
        );
        
        when(roomService.filterRoomsWithEventCount(null, null, "name")).thenReturn(rooms);

        String viewName = roomController.confirmDeleteRoom(1L, model, 100, "test", "name");

        verify(roomService).filterRoomsWithEventCount(null, null, "name");
        assert viewName.equals("redirect:/rooms?capacity=100&search=test&sort=name");
    }

    @Test
    public void testDeleteRoom_Success() {
        Room room = createRoom(1L, "Room to Delete", 50);
        room.setEvents(Collections.emptySet());
        Locale locale = Locale.ENGLISH;

        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

        try (MockedStatic<LocaleContextHolder> localeHolder = mockStatic(LocaleContextHolder.class)) {
            localeHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            when(messageSource.getMessage(eq("room.deleted"), any(), eq(locale))).thenReturn("Room deleted");

            String viewName = roomController.deleteRoom(1L, 100, "test", "name", redirectAttributes);

            verify(roomService).getRoomById(1L);
            verify(roomService).deleteById(1L);
            verify(redirectAttributes).addFlashAttribute("message", "Room deleted");
            assert viewName.equals("redirect:/rooms?capacity=100&search=test&sort=name");
        }
    }

    @Test
    public void testDeleteRoom_WithEvents() {
        Room room = createRoom(1L, "Room with Events", 50);
        List<Event> eventList = Arrays.asList(new Event(), new Event());
        room.setEvents(new HashSet<>(eventList));
        Locale locale = Locale.ENGLISH;

        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

        try (MockedStatic<LocaleContextHolder> localeHolder = mockStatic(LocaleContextHolder.class)) {
            localeHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            when(messageSource.getMessage(eq("room.delete.events"), any(), eq(locale))).thenReturn("Cannot delete room with events");

            String viewName = roomController.deleteRoom(1L, 100, "test", "name", redirectAttributes);

            verify(roomService).getRoomById(1L);
            verify(roomService, times(0)).deleteById(anyLong());
            verify(redirectAttributes).addFlashAttribute("error", "Cannot delete room with events");
            assert viewName.equals("redirect:/rooms?capacity=100&search=test&sort=name");
        }
    }

    @Test
    public void testDeleteRoom_NonExistentRoom() {
        when(roomService.getRoomById(999L)).thenReturn(Optional.empty());

        String viewName = roomController.deleteRoom(999L, 100, "test", "name", redirectAttributes);

        verify(roomService).getRoomById(999L);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        assert viewName.equals("redirect:/rooms?capacity=100&search=test&sort=name");
    }

    @Test
    public void testDeleteRoom_ServiceException() {
        Room room = createRoom(1L, "Room to Delete", 50);
        room.setEvents(Collections.emptySet());

        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));
        doThrow(new RuntimeException("Database error")).when(roomService).deleteById(1L);

        String viewName = roomController.deleteRoom(1L, 100, "test", "name", redirectAttributes);

        verify(roomService).getRoomById(1L);
        verify(roomService).deleteById(1L);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        assert viewName.equals("redirect:/rooms?capacity=100&search=test&sort=name");
    }

    @Test
    public void testBuildRedirectUrl_NoParams() {
        String url = invokePrivateMethod(roomController, "buildRedirectUrl",
                "redirect:/rooms", null, null, null);

        assert url.equals("redirect:/rooms");
    }

    @Test
    public void testBuildRedirectUrl_WithParams() {
        String url = invokePrivateMethod(roomController, "buildRedirectUrl",
                "redirect:/rooms", 100, "test", "name");

        assert url.equals("redirect:/rooms?capacity=100&search=test&sort=name");
    }

    @Test
    public void testBuildRedirectUrl_WithPartialParams() {
        String url = invokePrivateMethod(roomController, "buildRedirectUrl",
                "redirect:/rooms", 100, null, "name");

        assert url.equals("redirect:/rooms?capacity=100&sort=name");
    }

    private Room createRoom(Long id, String name, int capacity) {
        Room room = new Room();
        room.setId(id);
        room.setName(name);
        room.setCapacity(capacity);
        room.setEvents(new HashSet<>());
        return room;
    }

    private RoomWithEventCount createRoomWithEventCount(Long id, String name, int capacity, int eventCount) {
        return new RoomWithEventCount(id, name, capacity, (long) eventCount);
    }

    @SuppressWarnings("unchecked")
    private String invokePrivateMethod(Object object, String methodName, Object... args) {
        try {
            Class<?>[] parameterTypes = new Class<?>[args.length];

            if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        // This is a bit tricky but we can use parameter types based on method signature
                        if (i == 0) parameterTypes[i] = String.class; // baseUrl
                        else if (i == 1) parameterTypes[i] = Integer.class; // capacity
                        else if (i == 2) parameterTypes[i] = String.class; // search
                        else if (i == 3) parameterTypes[i] = String.class; // sort
                    } else {
                        parameterTypes[i] = args[i].getClass();
                    }
                }
            }

            java.lang.reflect.Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return (String) method.invoke(object, args);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking private method", e);
        }
    }
}