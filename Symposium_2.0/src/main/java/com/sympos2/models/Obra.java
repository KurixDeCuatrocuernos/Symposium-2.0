package com.sympos2.models;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This is the class to creates Obra objects, which represents Book and Article entities in database by the parameter: "tipo".
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 */
@Document(collection = "works")
public class Obra {
	
	@Id
	@Indexed(unique=true)
	private Long isbn;
	
	private LocalDate fechaPublicacion;

	private String titulo;
	
	private String autor;
	
	private String tipo;
	
	private String abstracto;
	
	private String lugar_publicacion;
	
	private List<String> temas;
	
	private String editorial;
	
	private int paginaini;
	
	private int paginafin;
	
	public Obra() { }
	
	/**
	 * Constructor for Comentario as the type Book
	 * @param isbn Long which represents the id of the book.
	 * @param fechaPublicacion LocalDate with the date of publication of the book.
	 * @param titulo String with the title of the book.
	 * @param autor String with the name of the book's author.
	 * @param tipo String to determinate the type of the Obra, in this case probably will be "BOOK".
	 * @param abstracto String with the resume of the book.
	 * @param lugar_publicacion String with the place where the book was published, in this case a country or city
	 * @param temas an ArrayList of Strings with the themes which the book could talk.
	 * @param editorial String with the name of the editorial which published the book.
	 */
	public Obra(Long isbn, LocalDate fechaPublicacion, String titulo, String autor, String tipo, String abstracto,
			String lugar_publicacion, List<String> temas, String editorial) {
		this.isbn = isbn;
		this.fechaPublicacion = fechaPublicacion;
		this.titulo = titulo;
		this.autor = autor;
		this.tipo = tipo.toUpperCase();
		this.abstracto = abstracto;
		this.lugar_publicacion = lugar_publicacion;
		this.temas = temas;
		this.editorial = editorial;
	}

	/**
	 * Constructor for Comentario as the type Article
	 * @param isbn Long which represents the id of the article.
	 * @param fechaPublicacion LocalDate with the date of publication of the article.
	 * @param titulo String with the title of the article.
	 * @param autor String with the name of the article's author.
	 * @param tipo String to determinate the type of the Obra, in this case probably will be "ARTICLE".
	 * @param abstracto String with the resume of the article.
	 * @param lugar_publicacion String with the place where the article was published, in this case a book or magazine.
	 * @param temas an ArrayList of Strings with the themes which the article could talk.
	 * @param editorial String with the name of the editorial which published the article or the book/magazine where the article was published.
	 */
	public Obra(Long isbn, LocalDate fechaPublicacion, String titulo, String autor, String tipo, String abstracto,
			String lugar_publicacion, List<String> temas, String editorial, int paginaini, int paginafin) {
		this.isbn = isbn;
		this.fechaPublicacion = fechaPublicacion;
		this.titulo = titulo;
		this.autor = autor;
		this.tipo = tipo.toUpperCase();
		this.abstracto = abstracto;
		this.lugar_publicacion = lugar_publicacion;
		this.temas = temas;
		this.editorial = editorial;
		this.paginaini = paginaini;
		this.paginafin = paginafin;
	}

	public Long getIsbn() {
		return isbn;
	}

	public void setIsbn(Long isbn) {
		this.isbn = isbn;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getAbstracto() {
		return abstracto;
	}

	public void setAbstracto(String abstracto) {
		this.abstracto = abstracto;
	}

	public String getLugar_publicacion() {
		return lugar_publicacion;
	}

	public void setLugar_publicacion(String lugar_publicacion) {
		this.lugar_publicacion = lugar_publicacion;
	}

	public List<String> getTemas() {
		return temas;
	}

	public void setTemas(List<String> temas) {
		this.temas = temas;
	}

	public String getEditorial() {
		return editorial;
	}

	public void setEditorial(String editorialOrPage) {
		this.editorial = editorialOrPage;
	}

	public LocalDate getFechaPublicacion() {
		return fechaPublicacion;
	}

	public void setFechaPublicacion(LocalDate fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}

	public int getPaginaini() {
		return paginaini;
	}

	public void setPaginaini(int paginaini) {
		this.paginaini = paginaini;
	}

	public int getPaginafin() {
		return paginafin;
	}

	public void setPaginafin(int paginafin) {
		this.paginafin = paginafin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(abstracto, autor, editorial, fechaPublicacion, isbn, lugar_publicacion, paginafin,
				paginaini, temas, tipo, titulo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Obra other = (Obra) obj;
		return Objects.equals(abstracto, other.abstracto) && Objects.equals(autor, other.autor)
				&& Objects.equals(editorial, other.editorial)
				&& Objects.equals(fechaPublicacion, other.fechaPublicacion) && isbn == other.isbn
				&& Objects.equals(lugar_publicacion, other.lugar_publicacion) && paginafin == other.paginafin
				&& paginaini == other.paginaini && Objects.equals(temas, other.temas)
				&& Objects.equals(tipo, other.tipo) && Objects.equals(titulo, other.titulo);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Obra [isbn=").append(isbn).append(", fechaPublicacion=").append(fechaPublicacion)
				.append(", titulo=").append(titulo).append(", autor=").append(autor).append(", tipo=").append(tipo)
				.append(", abstracto=").append(abstracto).append(", lugar_publicacion=").append(lugar_publicacion)
				.append(", temas=").append(temas).append(", editorial=").append(editorial).append(", paginaini=")
				.append(paginaini).append(", paginafin=").append(paginafin).append("]");
		return builder.toString();
	}

	
}