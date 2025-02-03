package com.sympos2.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.sympos2.models.Usuario;
/**
 * This Interface allows to use the different methods to see the Usuario params collected from the Database in different ways
 */
@Repository
public interface UserRepository extends MongoRepository<Usuario, String> {
	
	Optional<Usuario> findByEmail(String email);
	
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
	
}
