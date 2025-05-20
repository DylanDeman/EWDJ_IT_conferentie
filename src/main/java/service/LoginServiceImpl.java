package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private MessageSource messageSource;

	@Override
	public void prepareLoginModel(Model model, String error, String logout) {
		if (error != null) {
			String errorMessage = messageSource.getMessage("login.error", null, LocaleContextHolder.getLocale());
			model.addAttribute("error", errorMessage);
		}

		if (logout != null) {
			String logoutMessage = messageSource.getMessage("login.logout", null, LocaleContextHolder.getLocale());
			model.addAttribute("message", logoutMessage);
		}
	}
}