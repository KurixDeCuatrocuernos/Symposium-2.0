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
 * This Interface allows to use the different methods to see the Usuario params collected from the Database in different ways
 */
@Repository
public interface UserRepository extends MongoRepository<Usuario, String> {
	
	Optional<Usuario> findByEmail(String email);
	
	Optional<Usuario> findById(String id);
	
	@Query(value = "{ '_id': ?0 }", fields = "{'_id': 1, 'name': 1, 'role': 1 , 'school': 1 }")
	Optional<UsuarioComentarioPintado> findByIdOnlyIdAndNameAndRole(String id);
	
	@Query(value="{}", fields="{'_id':1}")
	List<Usuario> findAllOnlyId();
	
	Optional<Usuario> findIdByEmail(String email);
	
	void deleteById(String id);
	
	List<Usuario> findAllById(String id);
	
	List<Usuario> findAll();
	
	
	
	// PROYECTIONS
	
	@Query(value="{}", fields="{ email: 1}")
	List<Usuario> findAllEmailsOnly();
	
	@Query(value="{}", fields="email: 1, password: 1")
	Optional<Usuario> findByEmailAndPasswordOnly();
	
	@Query(value="{}", fields="{id: 0}")
	List<Usuario> findAllExcludeId();
	
	@Query(value="{}", fields="{id: 0, password: 0}")
	List<Usuario> findAllExcludeIdAndPassword();
	
	@Query(value="{ 'email' : ?0 }", fields="{ 'id' : 1 }")
	Optional<Usuario> findByEmailOnlyId(String email);

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
