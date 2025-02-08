package com.sympos2.models;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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

	public Obra(Long isbn, LocalDate fechaPublicacion, String titulo, String autor, String tipo, String abstracto,
			String lugar_publicacion, List<String> temas, String editorial) {
		this.isbn = isbn;
		this.fechaPublicacion = fechaPublicacion;
		this.titulo = titulo;
		this.autor = autor;
		this.tipo = tipo;
		this.abstracto = abstracto;
		this.lugar_publicacion = lugar_publicacion;
		this.temas = temas;
		this.editorial = editorial;
	}

	public Obra(Long isbn, LocalDate fechaPublicacion, String titulo, String autor, String tipo, String abstracto,
			String lugar_publicacion, List<String> temas, String editorial, int paginaini, int paginafin) {
		this.isbn = isbn;
		this.fechaPublicacion = fechaPublicacion;
		this.titulo = titulo;
		this.autor = autor;
		this.tipo = tipo;
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