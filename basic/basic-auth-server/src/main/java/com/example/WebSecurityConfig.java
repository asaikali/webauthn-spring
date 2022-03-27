package com.example;


import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		UserBuilder users = User.withDefaultPasswordEncoder();

		// add a regular user
		UserDetails user = users
				.username("user")
				.password("user")
				.roles("USER")
				.build();
		manager.createUser(user);

		// add an admin user
		UserDetails admin = users
				.username("admin")
				.password("admin")
				.roles("USER", "ADMIN")
				.build();
		manager.createUser(admin);

		return manager;
	}

}
