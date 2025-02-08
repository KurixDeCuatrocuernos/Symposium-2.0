package com.sympos2.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sympos2.models.Obra;

public interface ObraRepository extends MongoRepository<Obra, Long>{
	
	Optional<Obra> findByIsbn(Long isbn);
	
	Optional<Obra> deleteByIsbn(Long isbn);
	
	Optional<Obra> findByAutorAndTituloAndFechaPublicacion(String autor, String titulo, LocalDate fechaPublicacion);
	
	List<Obra> findAllByFechaPublicacion(LocalDate fechaPublicacion);
	
	List<Obra> findAllByTipo(String tipo);
	
	List<Obra> findAllByTitulo(String titulo);
	
	List<Obra> findAllByAutor(String autor);
	
	List<Obra> findAllByTemas(String temas);
	
	List<Obra> findAllByAutorAndTitulo(String autor, String titulo);
	
	List<Obra> findAllByEditorial(String editorial);
	
	@Query(value="{}", fields="{titulo:1, Autor:1}")
	List<Obra> findAllByTitleOnlyTituloAndAutor(String titulo);
	
	@Query(value="{}", fields="{titulo:1, Autor:1}")
	Optional<Obra> findByIsbnOnlyTituloAndAutor(Long isbn);
	
	
	
}
