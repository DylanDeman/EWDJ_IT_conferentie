package com.springboot.EWDJ_IT_conferentie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import service.RoomService;
import validation.RoomNameValidator;

import java.util.stream.Stream;

public class RoomNameValidatorTest {

    @Mock
    private RoomService roomService;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintViolationBuilder builder;

    @InjectMocks
    private RoomNameValidator validator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @MethodSource("validationTestCases")
    public void testIsValid(String roomName, boolean expectedResult, boolean existsByName, boolean shouldCallService) {
        if (shouldCallService) {
            when(roomService.existsByName(roomName)).thenReturn(existsByName);
            
            if (existsByName) {
                setupCustomConstraintViolation("Room name must be unique");
            }
        }

        boolean result = validator.isValid(roomName, context);
        
        assertEquals(expectedResult, result);
        
        if (shouldCallService) {
            verify(roomService).existsByName(roomName);
            
            if (existsByName) {
                verify(context).disableDefaultConstraintViolation();
                verify(context).buildConstraintViolationWithTemplate("Room name must be unique");
                verify(builder).addConstraintViolation();
            }
        } else {
            verifyNoInteractions(roomService);
        }
    }
    

    private void setupCustomConstraintViolation(String errorMessage) {

    doNothing().when(context).disableDefaultConstraintViolation();
    

    when(context.buildConstraintViolationWithTemplate(errorMessage)).thenReturn(builder);
    when(builder.addConstraintViolation()).thenReturn(context);
}
    
    private static Stream<Arguments> validationTestCases() {
        return Stream.of(
            Arguments.of(null, false, false, false),
            Arguments.of("", false, false, false),
            Arguments.of("A1234", false, false, false),
            Arguments.of("A123", true, false, true),
            Arguments.of("A123", false, true, true)
        );
    }
}