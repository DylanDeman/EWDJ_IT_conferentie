package com.springboot.EWDJ_IT_conferentie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private UserDetailsServiceImpl userService;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.ignoringRequestMatchers("/h2-console/**")).authorizeHttpRequests(requests -> requests
						// Static resources allowed without auth
						.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
						// Public pages allowed without auth (including /login and /error)
						.requestMatchers("/", "/login", "/error", "/events", "/events/{id}", "/register",
								"/h2-console/**")
						.permitAll()
						// Role-based access
						.requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/user/**", "/favorites/**")
						.hasRole("USER")
						// Everything else requires authentication
						.anyRequest().authenticated())
				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login").defaultSuccessUrl("/")
						.failureUrl("/login?error").permitAll())
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
						.logoutSuccessUrl("/").invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll())
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())
						.xssProtection(
								xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
						.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; "
								+ "frame-src 'self' data:; "
								+ "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; "
								+ "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
								+ "img-src 'self' data: https:; " + "font-src 'self' https://cdnjs.cloudflare.com"))
						.referrerPolicy(referrer -> referrer
								.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
				.sessionManagement(session -> session.maximumSessions(1).expiredUrl("/login?expired"));

		return http.build();
	}
}
