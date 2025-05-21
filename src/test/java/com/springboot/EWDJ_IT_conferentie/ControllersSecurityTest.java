package com.springboot.EWDJ_IT_conferentie;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

public class ControllersSecurityTest {

    @Test
    public void testAdminControllerSecurity() {
        Class<?> controllerClass = AdminController.class;

        verifyControllerAnnotation(controllerClass);
        verifyRequestMappingValue(controllerClass, "/admin");

        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(GetMapping.class) ||
                    method.isAnnotationPresent(PostMapping.class)) {

                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                if (annotation == null) {
                    // If method doesn't have annotation, check class-level annotation
                    annotation = controllerClass.getAnnotation(PreAuthorize.class);
                }

                assertNotNull(annotation,
                        "Admin controller method should have PreAuthorize annotation: " + method.getName());
                assertTrue(annotation.value().contains("hasRole('ADMIN')"),
                        "Admin controller method should require ADMIN role: " + method.getName());
            }
        }
    }

    @Test
    public void testUserControllerSecurity() {
        Class<?> controllerClass = UserController.class;

        verifyControllerAnnotation(controllerClass);
        verifyRequestMappingValue(controllerClass, "/user");

        Method favoritesMethod = findMethodByName(controllerClass, "listFavorites");
        assertNotNull(favoritesMethod, "listFavorites method should exist in UserController");

        PreAuthorize annotation = favoritesMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "listFavorites method should have PreAuthorize annotation");
        assertTrue(annotation.value().contains("hasRole('USER')"),
                "listFavorites method should require USER role");
    }

    @Test
    public void testRoomControllerSecurity() {
        Class<?> controllerClass = RoomController.class;

        verifyControllerAnnotation(controllerClass);
        verifyRequestMappingValue(controllerClass, "/rooms");

        Map<String, Boolean> adminMethods = new HashMap<>();
        adminMethods.put("showAddRoomForm", true);
        adminMethods.put("addRoom", true);
        adminMethods.put("confirmDeleteRoom", true);
        adminMethods.put("deleteRoom", true);


        adminMethods.put("listRooms", false);
        adminMethods.put("viewRoom", false);

        for (Method method : controllerClass.getDeclaredMethods()) {
            if (adminMethods.containsKey(method.getName())) {
                boolean shouldRequireAdmin = adminMethods.get(method.getName());

                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

                if (shouldRequireAdmin) {
                    assertNotNull(annotation,
                            "Admin method should have PreAuthorize annotation: " + method.getName());
                    assertTrue(annotation.value().contains("hasRole('ADMIN')"),
                            "Admin method should require ADMIN role: " + method.getName());
                } else if (annotation != null) {
                    // If not required but has annotation, make sure it doesn't restrict to ADMIN
                    assertFalse(annotation.value().contains("hasRole('ADMIN')"),
                            "Public method should not require ADMIN role: " + method.getName());
                }
            }
        }
    }

    @Test
    public void testFavoriteControllerSecurity() {
        Class<?> controllerClass = FavoriteController.class;

        verifyControllerAnnotation(controllerClass);
        verifyRequestMappingValue(controllerClass, "/events");

        Method addToFavoritesMethod = findMethodByName(controllerClass, "addToFavorites");
        Method removeFromFavoritesMethod = findMethodByName(controllerClass, "removeFromFavorites");

        assertNotNull(addToFavoritesMethod, "addToFavorites method should exist");
        assertNotNull(removeFromFavoritesMethod, "removeFromFavorites method should exist");

        // Favorite controller doesn't use PreAuthorize but checks authentication in the method
        // We can verify it has PostMapping annotations
        assertNotNull(addToFavoritesMethod.getAnnotation(PostMapping.class),
                "addToFavorites should have PostMapping annotation");
        assertNotNull(removeFromFavoritesMethod.getAnnotation(PostMapping.class),
                "removeFromFavorites should have PostMapping annotation");
    }

    @Test
    public void testLoginControllerSecurity() {
        Class<?> controllerClass = LoginController.class;

        verifyControllerAnnotation(controllerClass);
        verifyRequestMappingValue(controllerClass, "/login");

        Method loginMethod = findMethodByName(controllerClass, "login");
        assertNotNull(loginMethod, "login method should exist");

        // Login should not have PreAuthorize annotation as it needs to be publicly accessible
        PreAuthorize annotation = loginMethod.getAnnotation(PreAuthorize.class);
        assertNull(annotation, "login method should not have PreAuthorize annotation");
    }

    @Test
    public void testSecurityConfigAuthenticationRules() {
        Class<?> configClass = SecurityConfig.class;

        assertTrue(configClass.isAnnotationPresent(org.springframework.context.annotation.Configuration.class),
                "SecurityConfig should have @Configuration annotation");
        assertTrue(configClass.isAnnotationPresent(org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class),
                "SecurityConfig should have @EnableWebSecurity annotation");

        try {
            Method filterChainMethod = configClass.getDeclaredMethod("filterChain",
                    org.springframework.security.config.annotation.web.builders.HttpSecurity.class);
            assertNotNull(filterChainMethod, "filterChain method should exist");

            assertTrue(filterChainMethod.isAnnotationPresent(org.springframework.context.annotation.Bean.class),
                    "filterChain method should have @Bean annotation");
        } catch (NoSuchMethodException e) {
            fail("SecurityConfig should have filterChain method");
        }
    }



    private void verifyControllerAnnotation(Class<?> controllerClass) {
        assertNotNull(controllerClass.getAnnotation(Controller.class),
                controllerClass.getSimpleName() + " should have @Controller annotation");
    }

    private void verifyRequestMappingValue(Class<?> controllerClass, String expectedValue) {
        RequestMapping annotation = controllerClass.getAnnotation(RequestMapping.class);
        assertNotNull(annotation,
                controllerClass.getSimpleName() + " should have @RequestMapping annotation");

        String actualValue = annotation.value().length > 0 ? annotation.value()[0] : "";
        assertEquals(expectedValue, actualValue,
                controllerClass.getSimpleName() + " should have @RequestMapping(\"" + expectedValue + "\")");
    }

    private Method findMethodByName(Class<?> clazz, String methodName) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElse(null);
    }
}