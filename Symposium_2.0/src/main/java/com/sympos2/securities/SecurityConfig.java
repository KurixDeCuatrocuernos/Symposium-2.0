package com.sympos2.securities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This class configures the security Bean of Spring Web Security to grant access to some pages and deny to other, also edit the access credentials 
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Value("${spring.security.user.name}") // Collects the User's name from application.properties
	private String username; 
	
	@Value("${spring.security.user.password}") // Collects the User's password from application.properties
	private String password;
	
	@Value("${spring.security.user.roles}") // Collects the User's role from application.properties
	private String roles;
	
	/**
	 * Bean that provides an instance of AuthenticationManager.
	 * This manager is responsible for handling authentication requests.
	 *
	 * @param http The HttpSecurity configuration object.
	 * @param authConfig The AuthenticationConfiguration to retrieve the AuthenticationManager.
	 * @return The AuthenticationManager instance.
	 * @throws Exception If an error occurs during authentication configuration.
	 */
	@Bean
	AuthenticationManager authManager(HttpSecurity http, AuthenticationConfiguration authConfig) throws Exception{
			return authConfig.getAuthenticationManager();
	}
	
	/**
	 * Bean that configures the security filter chain for HTTP requests.
	 * Defines which end-points are publicly accessible and which require authentication.
	 * 
	 * @param http HttpSecurity object used to configure HTTP request security.
	 * @return A configured SecurityFilterChain.
	 * @throws Exception If any security configuration error occurs.
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http
			.authorizeHttpRequests(authorizeRequests -> 
				authorizeRequests
					.requestMatchers("/","/index").permitAll() // here we can add the URL we want to have a free access. 
					.anyRequest().authenticated()
				)
			.formLogin(formLogin -> 
				formLogin
//				.loginPage("/login") // If we didn't create a login template we don't need this function.
				.defaultSuccessUrl("/") // If the login is successful it redirects to the page you wanted (if you didn't want one, it redirects you to the main page), if you want force one page as default, add true after the string with the URL.  
				.permitAll()
				)
			.logout(logout ->
				logout.permitAll()
					);
		return http.build(); 
	}
	
	/**
	 * Bean that configures a custom UserDetailsService to retrieve user details based on the provided username.
	 * This method provides a custom implementation of the UserDetailsService interface, which is used
	 * by Spring Security to authenticate and load user-specific data.
	 * 
	 * @param passwordEncoder A PasswordEncoder to encode the user's password before storing it.
	 * @return A custom UserDetailsService implementation.
	 */
	@Bean
	UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
		return inputUsername -> {
			if(username.equals(inputUsername)) {
				return User.withUsername(username)
						   .password(passwordEncoder.encode(password)) // encodes the password before save it in memory
					       .roles(roles.split(",")) // .split is for if you add more than one role
					       .build(); // builds the User object with the elements we gave
			}
			throw new UsernameNotFoundException("User not Found"); // If credentials don't match we send UserNotFound error
		};
		
	}
	
	/**
	 * Bean that returns an instance of PasswordEncoder. It returns an instance of BCryptPasswordEncoder, which is used for 
	 * securely hashing and verifying passwords using the BCrypt algorithm.
	 * 
	 * @return an instance of BCryptPasswordEncoder
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}// End of the Class