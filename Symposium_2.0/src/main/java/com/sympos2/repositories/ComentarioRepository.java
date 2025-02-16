package com.sympos2.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.sympos2.dto.RespuestaComentario;
import com.sympos2.models.Comentario;

public interface ComentarioRepository extends MongoRepository<Comentario, String>{
	
	Optional<Comentario> findById(String id);
	
	Optional<Comentario> findByObraAndUsuario(Long obra, String usuario);
	
	Optional<RespuestaComentario> findByUsuarioAndTipo(String usuario, String tipo);
	
	List<Comentario> findAllByComment(String comment);
	
	List<Comentario> findAllByObra(Long obra);
	
	List<Comentario> findAllByObraAndTipo(Long obra, String tipo, Sort sort);
	
	List<Comentario> findAllByUsuario(String usuario);
	
	List<Comentario> findAllByObraAndUsuario(Long obra, String usuario);
	
	List<Comentario> findAllByTipo(String tipo);
	
	List<Comentario> findAllOrderByFecha();
	
}
