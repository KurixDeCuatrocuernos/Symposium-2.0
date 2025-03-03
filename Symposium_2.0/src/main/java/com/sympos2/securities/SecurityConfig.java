package com.sympos2.securities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sympos2.services.UserService;

/**
 * This class contains Beans to configure Spring Security.
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
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
	
	@Autowired
	private UserService usuarioService;
	
	/**
	 * This method initializes the User with the specified configurations.
	 * @param usuarioDetailsService UserService to use the methods of that service.
	 * @see UserService
	 */
	public SecurityConfig (UserService usuarioDetailsService) {
		this.usuarioService = usuarioDetailsService;
	}

	/**
	 * This Bean provides an instance of AuthenticationManager. Allows to login with an User object.
	 * This manager is responsible for handling authentication requests.
     * @param http The HttpSecurity configuration object.	 
     * @param passwordEncoder The PasswordEncoder that encrypt the passwords.
	 * @param userDetailsService UserService details to initialize the Security with an User object
	 * @param authConfig The AuthenticationConfiguration to retrieve the AuthenticationManager.
	 * @return The AuthenticationManager instance.
	 * @throws Exception If an error occurs during authentication configuration.
	 */
	@Bean
	AuthenticationManager authManager(HttpSecurity http, PasswordEncoder passwordEncoder, UserService userDetailsService, AuthenticationConfiguration authConfig) throws Exception {
	    return authConfig.getAuthenticationManager();
	}
	
	/**
	 * This Bean configures the security filter chain for HTTP requests.
	 * Defines which end-points are publicly accessible and which require authentication.
	 * 
	 * @param http HttpSecurity object used to configure HTTP request security.
	 * @return A configured SecurityFilterChain.
	 * @throws Exception If any security configuration error occurs.
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/", "/index/**", "/login/**", "/register/**", "/submit/**", "/form/**", "/workShow/**", "/App/**","/sugerencias/**","/getUserRole/**", "/getUserAvatar/**", "/getUsername", "/getLogin","/getLogout", "/getWriting/**", "/getComentarios", "/getAnswers", "/getUserIdent", "/getCommented", "/postCommentInserted", "/getIdComment", "/getCommentEdit", "/postCommentEdited", "/getEmails", "/postRegistryUser", "/getNewestWriting", "/getMostValuedWriting", "/getTitledComment", "/getStudentComment", "/getAllIdUsers", "/getUserDeleted", "/getEmailsEdit", "/getUserToEdit", "/postUserEdited", "/getAllIdWorks", "/getWorkDeleted", "/getWorkToEdit", "/postWorkEdited", "/getSearchWorkList", "/getSearchUsersList", "/geIsbnChecked", "/postWorkInsert", "/getBanComment", "/getAllBanComments", "/getUnbanComment", "/getDelComment", "/getSearchCommentList", "/chat/**").permitAll() // here we can add the URL we want to have a free access.
                                .requestMatchers("/admin-zone-users-list/**","/edit/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
		                        .loginPage("/login")  // Define la URL de la página de login personalizada
		                        .loginProcessingUrl("/login/submit") // <- Este es el punto importante
		                        .defaultSuccessUrl("/")  // Redirige al index después de un login exitoso
		                        .failureUrl("/login?error=true")  // URL a la que redirige si la autenticación falla
		                        .permitAll()  // Permite acceso sin autenticación a la página de login
                )
                .logout(logout ->
                        logout
                                .permitAll()
                                .logoutSuccessUrl("/")
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/")  // Redirige al inicio si no tiene permisos
                );
		return http.build(); 
	}
	
	/**
	 * This Bean returns an instance of PasswordEncoder. It returns an instance of BCryptPasswordEncoder, which is used for 
	 * securely hashing and verifying passwords using the BCrypt algorithm.
	 * 
	 * @return an instance of BCryptPasswordEncoder
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}// End of the Class