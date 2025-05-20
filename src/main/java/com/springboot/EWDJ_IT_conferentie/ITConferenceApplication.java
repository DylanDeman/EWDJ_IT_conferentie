package com.springboot.EWDJ_IT_conferentie;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import service.EventService;
import service.EventServiceImpl;
import service.RoomService;
import service.RoomServiceImpl;
import service.SpeakerService;
import service.SpeakerServiceImpl;
import service.UserDetailsServiceImpl;
import service.UserService;
import service.UserServiceImpl;
import validation.BeamerCheckValidator;
import validation.ConferencePeriodValidator;

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

	/* LOCALE */
	@Bean
	LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(new Locale("en", "BE"));// or any Euro zone locale
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

	/*
	 * @Bean MetricsService metricsService() { return new MetricsService(); }
	 */

	@Bean
	RoomService Roomservice() {
		return new RoomServiceImpl();
	}

	/* VALIDATOR */
	@Bean
	ConferencePeriodValidator ConferencePeriodValidator() {
		return new ConferencePeriodValidator();
	}

	@Bean
	BeamerCheckValidator BeamerCheckValidator() {
		return new BeamerCheckValidator();
	}

	@Bean
	HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}

}