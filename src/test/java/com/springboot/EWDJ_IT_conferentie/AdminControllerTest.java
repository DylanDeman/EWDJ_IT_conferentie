package com.springboot.EWDJ_IT_conferentie;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import service.AdminService;

public class AdminControllerTest {

    private AdminService adminService;
    private AdminController adminController;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        adminService = mock(AdminService.class);
        adminController = new AdminController();
        ReflectionTestUtils.setField(adminController, "adminService", adminService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                .build();
    }

    @Test
    public void testManageEvents_NoParameters() throws Exception {
        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events"));

        verify(adminService).storeAdminEventsUrl(any(HttpServletRequest.class), any(HttpSession.class));
        verify(adminService).prepareAdminEventsModel(any(Model.class), isNull(), isNull(), isNull(), isNull(), isNull(), eq("datetime"));
    }

    @ParameterizedTest
    @CsvSource({
            "2024-01-01,2024-12-31,1,100.0,workshop,price",
            "2024-05-15,,,,,"
    })
    public void testManageEvents_WithParameters(String dateFrom, String dateTo, Long room, Double priceMax, String search, String sort) throws Exception {
        LocalDate fromDate = dateFrom != null && !dateFrom.isEmpty() ? LocalDate.parse(dateFrom) : null;
        LocalDate toDate = dateTo != null && !dateTo.isEmpty() ? LocalDate.parse(dateTo) : null;

        mockMvc.perform(get("/admin/events")
                        .param(dateFrom != null && !dateFrom.isEmpty() ? "dateFrom" : "dummy", dateFrom != null && !dateFrom.isEmpty() ? dateFrom : "")
                        .param(dateTo != null && !dateTo.isEmpty() ? "dateTo" : "dummy", dateTo != null && !dateTo.isEmpty() ? dateTo : "")
                        .param(room != null ? "room" : "dummy", room != null ? room.toString() : "")
                        .param(priceMax != null ? "priceMax" : "dummy", priceMax != null ? priceMax.toString() : "")
                        .param(search != null ? "search" : "dummy", search != null ? search : "")
                        .param(sort != null ? "sort" : "dummy", sort != null ? sort : ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/events"));

        verify(adminService).storeAdminEventsUrl(any(HttpServletRequest.class), any(HttpSession.class));
        verify(adminService).prepareAdminEventsModel(
                any(Model.class),
                eq(fromDate),
                eq(toDate),
                eq(room),
                eq(priceMax),
                eq(search),
                eq(sort != null && !sort.isEmpty() ? sort : "datetime"));
    }

    @Test
    public void testManageEvents_DirectInvocation() {
        Model model = mock(Model.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 12, 31);
        Long room = 1L;
        Double priceMax = 99.99;
        String search = "java";
        String sort = "price";

        String viewName = adminController.manageEvents(model, request, session, dateFrom, dateTo, room, priceMax, search, sort);

        verify(adminService).storeAdminEventsUrl(request, session);
        verify(adminService).prepareAdminEventsModel(model, dateFrom, dateTo, room, priceMax, search, sort);
        assert "admin/events".equals(viewName);
    }

    @Test
    public void testManageEvents_DefaultSortValue() throws Exception {
        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk());

        verify(adminService).prepareAdminEventsModel(any(Model.class), isNull(), isNull(), isNull(), isNull(), isNull(), eq("datetime"));
    }

    @Test
    public void testManageEvents_EmptyStringParameters() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("search", "")
                        .param("sort", ""))
                .andExpect(status().isOk());

        verify(adminService).prepareAdminEventsModel(any(Model.class), isNull(), isNull(), isNull(), isNull(), eq(""), eq("datetime"));
    }

    @Test
    public void testManageEvents_MaxValues() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("room", Long.MAX_VALUE + "")
                        .param("priceMax", Double.MAX_VALUE + ""))
                .andExpect(status().isOk());

        verify(adminService).prepareAdminEventsModel(
                any(Model.class),
                isNull(),
                isNull(),
                eq(Long.MAX_VALUE),
                eq(Double.MAX_VALUE),
                isNull(),
                eq("datetime")
        );
    }

    @Test
    public void testSecurityAnnotation() {
        Class<AdminController> controllerClass = AdminController.class;

        boolean hasPreAuthorizeAnnotation = false;
        try {
            hasPreAuthorizeAnnotation = controllerClass.isAnnotationPresent(
                    org.springframework.security.access.prepost.PreAuthorize.class);

            if (!hasPreAuthorizeAnnotation) {
                String preAuthorizeValue = controllerClass
                        .getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class)
                        .value();
                assert preAuthorizeValue.equals("hasRole('ADMIN')");
            }
        } catch (Exception e) {
            // Reflection API might throw exceptions
        }

        assert hasPreAuthorizeAnnotation : "AdminController should have @PreAuthorize annotation";
    }
}