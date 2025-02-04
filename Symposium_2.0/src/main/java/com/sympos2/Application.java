package com.sympos2;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.sympos2.models.Usuario;
import com.sympos2.repositories.UserRepository;

@SpringBootApplication
@EnableMongoRepositories(basePackages="com.sympos2.repositories")
public class Application {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Application.class, args);
		var repo = context.getBean(UserRepository.class);
		repo.deleteAll();
		var user1 = new Usuario(null, "Juan", LocalDate.of(1998, 3, 4), "juan@correo.com", "12345", null, "estudiantePrueba", "escuelaPrueba");
		var user2 = new Usuario(null, "Elena", LocalDate.of(2002, 2, 6), "elena@correo.com", "12345", null, "estudiantePrueba", "escuelaPrueba");
		var user3 = new Usuario(null, "Lucas", LocalDate.of(2980, 2, 9), "lucas@correo.com", "12345", null, "estudiantePrueba", "escuelaPrueba");
		var user4 = new Usuario(null, "Adam", LocalDate.of(1996, 7, 9), "adam@correo.com", "12345", null, "estudiantePrueba", "escuelaPrueba");
		repo.saveAll(List.of(user1, user2, user3, user4));
		
		System.out.println(repo.findAll());
	}

}
