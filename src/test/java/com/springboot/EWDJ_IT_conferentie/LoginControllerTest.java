package com.springboot.EWDJ_IT_conferentie;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import service.LoginService;

public class LoginControllerTest {

    private LoginController loginController;
    private LoginService loginService;
    private Model model;

    @BeforeEach
    public void setup() {
        loginController = new LoginController();
        loginService = mock(LoginService.class);
        model = mock(Model.class);

        ReflectionTestUtils.setField(loginController, "loginService", loginService);
    }

    @Test
    public void testLogin_NoParameters() {
        String viewName = loginController.login(null, null, model);

        verify(loginService).prepareLoginModel(model, null, null);
        verifyNoMoreInteractions(loginService);
        assert "login".equals(viewName);
    }

    @Test
    public void testLogin_WithErrorParameter() {
        String viewName = loginController.login("true", null, model);

        verify(loginService).prepareLoginModel(model, "true", null);
        verifyNoMoreInteractions(loginService);
        assert "login".equals(viewName);
    }

    @Test
    public void testLogin_WithLogoutParameter() {
        String viewName = loginController.login(null, "true", model);

        verify(loginService).prepareLoginModel(model, null, "true");
        verifyNoMoreInteractions(loginService);
        assert "login".equals(viewName);
    }

    @Test
    public void testLogin_WithBothParameters() {
        String viewName = loginController.login("true", "true", model);

        verify(loginService).prepareLoginModel(model, "true", "true");
        verifyNoMoreInteractions(loginService);
        assert "login".equals(viewName);
    }

    @Test
    public void testLogin_WithDifferentParameterValues() {
        String viewName = loginController.login("invalid_credentials", "success", model);

        verify(loginService).prepareLoginModel(model, "invalid_credentials", "success");
        verifyNoMoreInteractions(loginService);
        assert "login".equals(viewName);
    }
}