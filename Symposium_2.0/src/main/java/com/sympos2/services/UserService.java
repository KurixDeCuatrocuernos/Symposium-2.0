package com.sympos2.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.sympos2.models.Usuario;
import com.sympos2.repositories.UserRepository;

/**
 * This class generates a MongoTemplate to use the Service
 */
@Service
public class UserService {
	
	@Autowired
	private MongoTemplate mt; 
	
	@Autowired
	private UserRepository userRepo;
	
	public Usuario edit(Usuario edit) {
		boolean encontrado=false;
		List<Usuario> repository = new ArrayList();
		repository = userRepo.findAll();
		int i=0;
		while (!encontrado && i<repository.size()) {
			if (repository.get(i).getId() == edit.getId()) {
				encontrado=true;
				userRepo.deleteById(repository.get(i).getId());
				userRepo.save(repository.get(i));
			} else {
				i++;
			}
		}
		
		if (!encontrado) {userRepo.save(edit);}
		
		return edit;
	}
}
