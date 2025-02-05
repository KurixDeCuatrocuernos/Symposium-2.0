package com.sympos2.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
		System.out.println("Se modificará: "+edit.getId());
		Optional<Usuario> user = userRepo.findById(edit.getId());
		
		if(!user.isPresent()) {
			userRepo.deleteById(user.get().getId());
			userRepo.save(user.get());
		} else {userRepo.save(edit);}
		
		return edit;
	}
}
