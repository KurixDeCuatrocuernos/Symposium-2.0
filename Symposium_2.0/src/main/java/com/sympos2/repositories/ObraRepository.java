package com.sympos2.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sympos2.dto.ObraIsbnTituloProjection;
import com.sympos2.models.Obra;

public interface ObraRepository extends MongoRepository<Obra, Long>{
	
	Optional<Obra> findByIsbn(Long isbn);
	
	Optional<Obra> deleteByIsbn(Long isbn);
	
	@Query(value = "{}", sort = "{'createdAt': -1}")
	List<Obra> findOneByOrderByCreatedAtDesc();
	
	Optional<Obra> findByAutorAndTituloAndFechaPublicacion(String autor, String titulo, LocalDate fechaPublicacion);
	
	List<Obra> findAllByFechaPublicacion(LocalDate fechaPublicacion);
	
	List<Obra> findAllByTipo(String tipo);
	
	List<Obra> findAllByTitulo(String titulo);
	
	List<Obra> findAllByAutor(String autor);
	
	List<Obra> findAllByTemas(String temas);
	
	List<Obra> findAllByAutorAndTitulo(String autor, String titulo);
	
	List<Obra> findAllByEditorial(String editorial);
	
	@Query(value="{}", fields="{titulo:1, Autor:1}")
	Optional<Obra> findByIsbnOnlyTituloAndAutor(Long isbn);
	
	@Query(value="{}", fields="{titulo:1, Autor:1}")
	List<Obra> findAllByTitleOnlyTituloAndAutor(String titulo);
		
	@Query(value= "{}", fields="{ 'isbn': 1, 'titulo' : 1}")
	List<ObraIsbnTituloProjection> findAllIsbnAndTitulo();
	
	@Query(value= "{}", fields="{ 'isbn': 1 }")
	List<Obra> findAllOnlyIsbn();
	
    @Query("{ '$or': [ " +
            "{ 'isbn': { '$regex': ?0, '$options': 'i' } }, " +  // Para isbn, búsqueda insensible a mayúsculas y minúsculas
            "{ 'titulo': { '$regex': ?0, '$options': 'i' } }, " + // Para titulo
            "{ 'autor': { '$regex': ?0, '$options': 'i' } }, " +  // Para autor
            "{ 'tipo': { '$regex': ?0, '$options': 'i' } }, " +   // Para tipo
            "{ 'abstracto': { '$regex': ?0, '$options': 'i' } }, " +  // Para abstracto
            "{ 'lugar_publicacion': { '$regex': ?0, '$options': 'i' } }, " + // Para lugar_publicacion
            "{ 'editorial': { '$regex': ?0, '$options': 'i' } } " +  // Para editorial
            "] }")
    List<Obra> findAllParams(String search);
}
