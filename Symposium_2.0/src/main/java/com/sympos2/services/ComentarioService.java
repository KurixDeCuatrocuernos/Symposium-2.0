package com.sympos2.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.sympos2.models.Comentario;
import com.sympos2.repositories.ComentarioRepository;

@Service
public class ComentarioService {
	
	@Autowired
	private ComentarioRepository commentRepo;
	
	@Autowired
	private MongoTemplate mt;
	
	public void banComment(String id) {
		Optional<Comentario> comment = commentRepo.findById(id);
		if (comment != null && !comment.isEmpty()) {
			if(comment.get().getTipo().equals("COMMENT")) {
				comment.get().setTipo("BANNED_COMMENT");
				commentRepo.deleteById(id);
				commentRepo.save(comment.get());
			} else {
				comment.get().setTipo("BANNED_ANSWER");
				commentRepo.deleteById(id);
				commentRepo.save(comment.get());
			}
		} else {
			System.out.println("No se encontró el comentario para bannearlo");
		}
	}
	
	public void unbanComment(String id) {
		Optional<Comentario> comment = commentRepo.findById(id);
		if (comment != null && !comment.isEmpty()) {
			if(comment.get().getTipo().equals("BANNED_COMMENT")) {
				comment.get().setTipo("COMMENT");
				commentRepo.deleteById(id);
				commentRepo.save(comment.get());
			} else {
				comment.get().setTipo("ANSWER");
				commentRepo.deleteById(id);
				commentRepo.save(comment.get());
			}
		} else {
			System.out.println("No se encontró el comentario para bannearlo");
		}
	}
	
}
