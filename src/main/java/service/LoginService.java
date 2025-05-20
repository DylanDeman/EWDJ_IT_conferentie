package service;

import org.springframework.ui.Model;

public interface LoginService {
	void prepareLoginModel(Model model, String error, String logout);
}