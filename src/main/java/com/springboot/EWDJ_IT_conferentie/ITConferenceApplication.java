package com.springboot.EWDJ_IT_conferentie;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import service.AdminService;
import service.AdminServiceImpl;
import service.EventService;
import service.EventServiceImpl;
import service.FavoriteService;
import service.FavoriteServiceImpl;
import service.LoginService;
import service.LoginServiceImpl;
import service.RoomService;
import service.RoomServiceImpl;
import service.SpeakerService;
import service.SpeakerServiceImpl;
import service.UserDetailsServiceImpl;
import service.UserService;
import service.UserServiceImpl;
import service.ValidationService;
import service.ValidationServiceImpl;
import validation.EventValidator;

@SpringBootApplication
@EnableJpaRepositories("repository")
@EntityScan("domain")
// @ComponentScan({"controllers", "config", "service"})
public class ITConferenceApplication implements WebMvcConfigurer {
	public static void main(String[] args) {
		SpringApplication.run(ITConferenceApplication.class, args);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/dashboard");
		registry.addViewController("/error").setViewName("error");
	}

	@Bean
	LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(new Locale("en", "BE"));
		return slr;
	}

	/* SERVICE */
	@Bean
	UserService UserService() {
		return new UserServiceImpl();
	}

	@Bean
	UserDetailsServiceImpl UserDetailsServiceImpl() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	EventService EventServiceImpl() {
		return new EventServiceImpl();
	}

	@Bean
	SpeakerService SpeakerServiceImpl() {
		return new SpeakerServiceImpl();
	}

	@Bean
	AdminService AdminServiceImpl() {
		return new AdminServiceImpl();
	}

	@Bean
	FavoriteService FavoriteServiceImpl() {
		return new FavoriteServiceImpl();
	}

	@Bean
	LoginService LoginServiceImpl() {
		return new LoginServiceImpl();
	}

	@Bean
	ValidationService ValidationServiceImpl() {
		return new ValidationServiceImpl();
	}

	@Bean
	RoomService Roomservice() {
		return new RoomServiceImpl();
	}

	/* VALIDATOR */

	@Bean
	EventValidator EventValidator() {
		return new EventValidator();
	}

}