package com.sympos2.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sympos2.dto.RespuestaComentario;
import com.sympos2.models.Comentario;

/**
 * This interface manages Comentario objects from Database.
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see MongoRepository interface
 */
public interface ComentarioRepository extends MongoRepository<Comentario, String>{
	
	/**
	 * This method finds a Comentario object in database by its Id. 
	 * @param id String with the Id which will be compared to found the work.
	 * @return returns an Optional object, filled with a Comentario object if it founds the comment in database, or an empty Optional if not.
	 */
	Optional<Comentario> findById(String id);
	
	/**
	 * This method finds a Comentario object in database by the work and user related. 
	 * @param obra Long with the id of the Work which the comment to find comments.
	 * @param usuario String with the id of the user who created the comment to find.
	 * @param tipo String with the comment's type, to find the comment.
	 * @return returns an Optional object, filled with a Comentario object if it founds the comment in database, or an empty Optional if not.
	 */
	Optional<Comentario> findByObraAndUsuarioAndTipo(Long obra, String usuario, String tipo);
	
	/**
	 * This method finds a Comentario object in database by the user who created it and the type of the comment (Comment or Answer). 
	 * @param usuario String with the id of the user who created the comment to find.
	 * @param tipo String with the type of the comment to find. 
	 * @return returns an Optional object, filled with a Comentario object if it founds the comment in database, or an empty Optional if not.
	 */
	Optional<RespuestaComentario> findByUsuarioAndTipo(String usuario, String tipo);
	
	/**
	 * This method finds all the Comentario objects in database by the comment which answers. 
	 * @param comment String with the comment's id to find the answers related. 
	 * @return returns a List of Comentario object, filled with a Comentario objects if founds one or more, or an empty Optional if not.
	 */
	List<Comentario> findAllByComment(String comment);
	
	/**
	 * This method finds all the Comentario objects in database by the Obra object it comments. 
	 * @param obra Long with the isbn of the Work that the comment comments.
	 * @return returns a List of Comentario object, filled with a Comentario objects if founds one or more, or an empty List if not.
	 */
	List<Comentario> findAllByObra(Long obra);
	
	/**
	 * This method finds all the Comentario objects in database by the Obra object it comments and the type of the comments ("COMMENT" or "ANSWER"), and order the results by a Sort object.
	 * @param obra Long with the isbn of the Work that the comment comments.
	 * @param tipo String with the type of the comment to find (COMMENT or ANSWER). 
	 * @param sort Sort to order the received data. 
	 * @return returns a sorted List of Comentario object, filled with a Comentario objects if founds one or more, or an empty List if not.
	 */
	List<Comentario> findAllByObraAndTipo(Long obra, String tipo, Sort sort);
	
	/**
	 * This method finds all the Comentario objects in database by the Obra object it comments, its type, the comment it comments, because is an Answer, and sorts the results by a Sort object.
	 * @param obra Long with the isbn of the Work that the comment comments.
	 * @param tipo String with the type of the comment to find (COMMENT or ANSWER). 
	 * @param comment String with the comment that the Comentario object comments (because is an Answer). 
	 * @param sort Sort to order the received data. 
	 * @return returns a sorted List of Comentario object, filled with a Comentario objects if founds one or more, or an empty List if not.
	 */
	List<Comentario> findAllByObraAndTipoAndComment(Long obra, String tipo, String comment, Sort sort);
	
	/**
	 * This method finds all the Comentario objects in database by the user who created it.
	 * @param usuario String with the id of the user who created the comment to find.
	 * @return returns a List of Comentario object, filled with a Comentario objects if founds one or more, or an empty List if not.
	 */
	List<Comentario> findAllByUsuario(String usuario);
	
	/**
	 * This method finds all the Comentario objects in database by the user who created it and the Obra object which comments.
	 * @param obra Long with the isbn of the Work that the comment comments.
	 * @param usuario String with the id of the user who created the comment to find.
	 * @return returns a List of Comentario object, filled with a Comentario objects if founds one or more, or an empty List if not.
	 */
	List<Comentario> findAllByObraAndUsuario(Long obra, String usuario);
	
	/**
	 * This method finds all the Comentario objects in database by its type (COMMENT or ANSWER).
	 * @param tipo String with the type of the comment to find (COMMENT or ANSWER). 
	 * @return returns a List of Comentario object, filled with a Comentario objects if founds one or more, or an empty List if not.
	 */
	List<Comentario> findAllByTipo(String tipo);
	
	/**
	 * This method finds all the Comentario objects in database sorting its results by fecha parameter.
	 * @return returns a sorted List of Comentario object, filled with a Comentario objects if founds one or more, or an empty List if not.
	 */
	List<Comentario> findAllOrderByFecha();
	
	/**
	 * This method finds all Comentario objects in database by its type and sorting it by fecha parameter.
	 * @param tipo String with the type of the comment to find (COMMENT or ANSWER). 
	 * @return returns a sorted List of Comentario object, filled with a Comentario objects if founds one or more, or an empty List if not.
	 */
	@Query(value = "{}", sort = "{'fecha': -1}")
	List<Comentario> findNewestCommentByFechaAndTipo(String tipo);
	
	/**
	 * This method finds all the Comentario objects of the type "COMMENT" in database by the Obra object which comments. And only shows the Valoracion parameter. 
	 * @param obra Long with the isbn of the Work that the comment comments.
	 * @return returns a List of Comentario object, filled with a Comentario objects, but only its valoracion parameter, if founds one or more in database, or an empty List if not.
	 */
	@Query(value="{ 'obra' : ?0, 'tipo' : 'COMMENT' }", fields="{ 'valoracion' : 1 }")
	List<Comentario> findAllByObraOnlyComment(Long obra);
	
	/**
	 * This method finds all the Comentario objects in database by any coincidence with the String. 
	 * @param search String with a text to find any coincidence in the database.
	 * @return returns a List of Comentario object, filled with a Comentario objects if founds one or more, or an empty List if not.
	 */
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
