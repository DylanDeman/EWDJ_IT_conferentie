package com.springboot.EWDJ_IT_conferentie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import domain.Room;
import service.RoomService;
import validation.RoomValidator;

import java.util.stream.Stream;

public class RoomValidatorTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomValidator roomValidator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @ValueSource(classes = {Room.class, Object.class, String.class})
    public void testSupports(Class<?> clazz) {
        assertEquals(clazz == Room.class, roomValidator.supports(clazz));
    }

    @Test
    public void testValidate_ValidRoom() {
        Room room = new Room();
        room.setId(1L);
        room.setName("A123");
        room.setCapacity(30);

        Errors errors = new BeanPropertyBindingResult(room, "room");
        roomValidator.validate(room, errors);
        
        assertFalse(errors.hasErrors());
    }

    @ParameterizedTest
    @MethodSource("invalidRoomTestCases")
    public void testValidate_InvalidRoom(Room room, String expectedErrorCode, String expectedField, boolean existsByName) {
        if (expectedErrorCode.equals("room.name.unique")) {
            when(roomService.existsByName(room.getName())).thenReturn(existsByName);
        }

        Errors errors = new BeanPropertyBindingResult(room, "room");
        roomValidator.validate(room, errors);
        
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(expectedErrorCode, errors.getFieldError(expectedField).getCode());
        
        if (expectedErrorCode.equals("room.name.unique")) {
            verify(roomService, times(1)).existsByName(room.getName());
        }
    }
    
    private static Stream<Arguments> invalidRoomTestCases() {
        Room roomWithInvalidName = new Room();
        roomWithInvalidName.setId(1L);
        roomWithInvalidName.setName("a123"); // lowercase letter, should be uppercase
        roomWithInvalidName.setCapacity(30);
        
        Room roomWithInvalidCapacity = new Room();
        roomWithInvalidCapacity.setId(1L);
        roomWithInvalidCapacity.setName("A123");
        roomWithInvalidCapacity.setCapacity(60); // above maximum 50
        
        Room roomWithDuplicateName = new Room();
        roomWithDuplicateName.setName("A123");
        roomWithDuplicateName.setCapacity(30);
        
        return Stream.of(
            Arguments.of(roomWithInvalidName, "room.name.format", "name", false),
            Arguments.of(roomWithInvalidCapacity, "room.capacity.range", "capacity", false),
            Arguments.of(roomWithDuplicateName, "room.name.unique", "name", true)
        );
    }
}