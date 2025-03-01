package com.sympos2.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sympos2.dto.RespuestaComentario;
import com.sympos2.models.Comentario;

public interface ComentarioRepository extends MongoRepository<Comentario, String>{
	
	Optional<Comentario> findById(String id);
	
	Optional<Comentario> findByObraAndUsuario(Long obra, String usuario);
	
	Optional<RespuestaComentario> findByUsuarioAndTipo(String usuario, String tipo);
	
	List<Comentario> findAllByComment(String comment);
	
	List<Comentario> findAllByObra(Long obra);
	
	List<Comentario> findAllByObraAndTipo(Long obra, String tipo, Sort sort);
	
	List<Comentario> findAllByObraAndTipoAndComment(Long obra, String tipo, String comment, Sort sort);
	
	List<Comentario> findAllByUsuario(String usuario);
	
	List<Comentario> findAllByObraAndUsuario(Long obra, String usuario);
	
	List<Comentario> findAllByTipo(String tipo);
	
	List<Comentario> findAllOrderByFecha();
	
	@Query(value = "{}", sort = "{'fecha': -1}")
	List<Comentario> findNewestCommentByFechaAndTipo(String tipo);
	
	@Query(value="{ 'obra' : ?0, 'tipo' : 'COMMENT' }", fields="{ 'valoracion' : 1 }")
	List<Comentario> findAllByObraOnlyComment(Long obra);
	
	@Query("{ '$or': [ " +
	        "{ '$expr': { '$regexMatch': { 'input': { '$toString': '$_id' }, 'regex': ?0, 'options': 'i' } } }, " + 
	        "{ 'titulo': { '$regex': ?0, '$options': 'i' } }, " + 
	        "{ 'texto': { '$regex': ?0, '$options': 'i' } }, " + 
	        "{ 'tipo': { '$regex': ?0, '$options': 'i' } }, " + 
	        "{ 'usuario': { '$regex': ?0, '$options': 'i' } }, " + 
	        "{ 'comment': { '$regex': ?0, '$options': 'i' } }, " + 
	        "{ 'obra': { '$regex': ?0, '$options': 'i' } }, " +
	        "{ '$expr': { '$regexMatch': { 'input': { '$toString': '$fecha' }, 'regex': ?0, 'options': 'i' } } }" +
	        "] }")
	List<Comentario> findAllParams(String search);
	
}
