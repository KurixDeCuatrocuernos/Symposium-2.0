package com.sympos2.repositories;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.sympos2.dto.UsuarioComentarioPintado;
import com.sympos2.models.Usuario;
/**
 * This Interface manages the User objects from database.
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see MongoRepository interface
 */
@Repository
public interface UserRepository extends MongoRepository<Usuario, String> {
	
	/**
	 * This method finds an User object from database by its email.
	 * @param email String with the email to find the user.
	 * @return returns an Optional object, it contains an User object if founds one, or contains empty if not.
	 */
	Optional<Usuario> findByEmail(String email);
	
	/**
	 * This method finds an User object from database by its id.
	 * @param id String with the id of the user to find.
	 * @return returns an Optional object, it contains an User object if founds one, or contains empty if not.
	 */
	Optional<Usuario> findById(String id);
	
	/**
	 * This method finds an User object from database by its id, but only shows its id, name, role and school 
	 * @param id String with the id of the user to find.
	 * @return returns an Optional object, it contains an User object if founds one, or contains empty if not.
	 */
	@Query(value = "{ '_id': ?0 }", fields = "{'_id': 1, 'name': 1, 'role': 1 , 'school': 1 }")
	Optional<UsuarioComentarioPintado> findByIdOnlyIdAndNameAndRole(String id);
	
	/**
	 * This method finds all User objects in database, but only shows its id.
	 * @return returns a List of User objects if founds one or more, or an empty List if not.
	 */
	@Query(value="{}", fields="{'_id':1}")
	List<Usuario> findAllOnlyId();
	
	/**
	 * This method finds an User object from database by its email, but only shows its email.
	 * @param email String with the email to find the user.
	 * @return returns an Optional object, it contains an User object if founds one, or contains empty if not.
	 */
	@Query(value="{ 'email': ?0 }", fields="{'_id':1}")
	Optional<Usuario> findIdByEmail(String email);
	
	/**
	 * This method finds all User objects in database, but only shows its email.
	 * @return returns a List of User objects if founds one or more, or an empty List if not.
	 */
	@Query(value="{}", fields="{ email: 1}")
	List<Usuario> findAllEmailsOnly();
	
	/**
	 * This method finds an User object from database, but only shows its id.
	 * @param email String with the email to find the user.
	 * @return returns an Optional object, it contains an User object if founds one, or contains empty if not.
	 */
	@Query(value="{ 'email' : ?0 }", fields="{ 'id' : 1 }")
	Optional<Usuario> findByEmailOnlyId(String email);
	
	/**
	 * This method finds all User objects in database by its role, but only shows its id.
	 * @param role String with the role of the User to find.
	 * @return returns a List of User objects if founds one or more, or an empty List if not.
	 */
	@Query(value="{ 'role' : ?0 }", fields="{ 'id' : 1 }")
	List<Usuario> findAllByRoleOnlyId(String role);
	
	/**
	 * This method finds all User objects in database by any coincidence with the search input.
	 * @param search String with the text to find any coincidence.
	 * @return returns a List of User objects if founds one or more, or an empty List if not.
	 */
	@Query("{ '$or': [ " +
	        "{ '$expr': { '$regexMatch': { 'input': { '$toString': '$_id' }, 'regex': ?0, 'options': 'i' } } }, " + // Búsqueda por subcadena en _id
	        "{ 'name': { '$regex': ?0, '$options': 'i' } }, " +   
	        "{ 'email': { '$regex': ?0, '$options': 'i' } }, " +  
	        "{ 'role': { '$regex': ?0, '$options': 'i' } }, " +  
	        "{ 'studies': { '$regex': ?0, '$options': 'i' } }, " +
	        "{ 'school': { '$regex': ?0, '$options': 'i' } }, " + 
	        "{ 'phone': { '$regex': ?0, '$options': 'i' } }, " +  
	        "{ 'studies_title': { '$regex': ?0, '$options': 'i' } }, " +  
	        "{ 'study_place': { '$regex': ?0, '$options': 'i' } }, " +
	        "{ '$expr': { '$regexMatch': { 'input': { '$toString': '$fechaNac' }, 'regex': ?0, 'options': 'i' } } }" +  // Búsqueda por fecha
	        "] }")
	List<Usuario> findAllParams(String search);
	
}
