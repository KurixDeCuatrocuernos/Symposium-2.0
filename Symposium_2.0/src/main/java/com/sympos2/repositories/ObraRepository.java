package com.sympos2.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sympos2.dto.ObraIsbnTituloProjection;
import com.sympos2.models.Obra;

/**
 * This interface manages the Obra objects from database.
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see MongoRepository interface
 */
public interface ObraRepository extends MongoRepository<Obra, Long>{
	
	/**
	 * This method finds an Obra object from database by its id (in this case isbn).
	 * @param isbn Long with the Obra's id.
	 * @return returns an Optional object, it contains an Obra object if founds one, or empty Optional if not.
	 */
	Optional<Obra> findByIsbn(Long isbn);
	
	/**
	 * This method deletes an Obra object from database by its id (in this case isbn).
	 * @param isbn Long with the Obra's id.
	 * @return returns an Optional object, it contains an Obra object if founds one, or empty Optional if not.
	 */
	Optional<Obra> deleteByIsbn(Long isbn);
	
	/**
	 * This method returns the last Obra object inserted in database.
	 * @return returns a sorted List of Obra object, it contains Obra objects if finds one or more, or empty if not.
	 */
	@Query(value = "{}", sort = "{'createdAt': -1}")
	List<Obra> findOneByOrderByCreatedAtDesc();
	
	/**
	 * This method finds a Obra object from database by its author, title and publication date.
	 * @param autor String with the author's name.
	 * @param titulo String with the title of the Obra object to find.
	 * @param fechaPublicacion LocalDate with the publication date of the Obra object to find.
	 * @return returns an Optional object, it contains an Obra object if founds one, or empty Optional if not.
	 */
	Optional<Obra> findByAutorAndTituloAndFechaPublicacion(String autor, String titulo, LocalDate fechaPublicacion);
	
	/**
	 * This method finds all Obra objects in database by its publication date. 
	 * @param fechaPublicacion LocalDate with the publication date of the Obra object to find.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	List<Obra> findAllByFechaPublicacion(LocalDate fechaPublicacion);
	
	/**
	 * This method finds all Obra objects in database by its type.
	 * @param tipo String with the type of the Obra object to find.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	List<Obra> findAllByTipo(String tipo);
	
	/**
	 * This method finds all Obra objects in database by its title.
	 * @param titulo String with the title of the Obra object to find.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	List<Obra> findAllByTitulo(String titulo);
	
	/**
	 * This method finds all Obra object in database by its author.
	 * @param autor String with the author's name.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	List<Obra> findAllByAutor(String autor);
	
	/**
	 * This method finds all Obra objects in database by its themes.
	 * @param temas String with the theme to find the Obra object.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	List<Obra> findAllByTemas(String temas);
	
	/**
	 * This method finds all Obra objects in database by its author and title.
	 * @param autor String with the author's name.
	 * @param titulo String with the title of the Obra object to find.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	List<Obra> findAllByAutorAndTitulo(String autor, String titulo);
	
	/**
	 * This method finds all Obra objects in database by its editorial.
	 * @param editorial String with the editorial to find the Obra objects.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	List<Obra> findAllByEditorial(String editorial);
	
	/**
	 * This method finds an Obra object from database by its id (isbn), but only shows its title and author.
	 * @param isbn Long with the Obra's id.
	 * @return returns an Optional object, it contains an Obra object if founds one, or empty Optional if not.
	 */
	@Query(value="{}", fields="{titulo:1, Autor:1}")
	Optional<Obra> findByIsbnOnlyTituloAndAutor(Long isbn);
	
	/**
	 * This method finds all the Obra objects in database by its title, but only shows its title and author.
	 * @param titulo String with the title of the Obra object to find.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	@Query(value="{}", fields="{titulo:1, Autor:1}")
	List<Obra> findAllByTitleOnlyTituloAndAutor(String titulo);
		
	/**
	 * This method finds all the Obra objects in database but only shows its id (isbn) and title.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	@Query(value= "{}", fields="{ 'isbn': 1, 'titulo' : 1}")
	List<ObraIsbnTituloProjection> findAllIsbnAndTitulo();
	
	/**
     * This method finds all the Obra objects in database but only shows its id (isbn).
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
	@Query(value= "{}", fields="{ 'isbn': 1 }")
	List<Obra> findAllOnlyIsbn();
	
	/**
	 * This method finds all the Obra objects in database by any coincidence with the search input.
	 * @param search String with the input to find any coincidence.
	 * @return returns a List of Obra object if founds one or more, or is empty if not.
	 */
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
