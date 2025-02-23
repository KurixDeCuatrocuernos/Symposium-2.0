package com.sympos2.models;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="coments")
public class Comentario {
	
	@Id
	private String id;
	
	private String titulo;
	
	private String texto;
	
	private LocalDateTime fecha;
	
	private int valoracion;
	
	private String tipo;
	
	private Long obra;
	
	// these are optional
	
	private String usuario;
	
	private String comment;
	
	public Comentario() { }

	public Comentario(String id, String titulo, String texto, LocalDateTime fecha, int valoracion, String tipo,
			Long obra, String usuario) {
		this.id = id;
		this.titulo = titulo;
		this.texto = texto;
		this.fecha = fecha;
		this.valoracion = valoracion;
		this.tipo = tipo.toUpperCase();
		this.obra = obra;
		this.usuario = usuario;
	}
	
	// Constructor for Respuesta
	
	public Comentario(String id, String texto, LocalDateTime fecha, String tipo, Long obra,
			String usuario, String comment) {
		this.id = id;
		this.texto = texto;
		this.fecha = fecha;
		this.tipo = tipo.toUpperCase();
		this.obra = obra;
		this.usuario = usuario;
		this.comment = comment;
	}
	
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public int getValoracion() {
		return valoracion;
	}

	public void setValoracion(int valoracion) {
		this.valoracion = valoracion;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Long getObra() {
		return obra;
	}

	public void setObra(Long obra) {
		this.obra = obra;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment.toUpperCase();
	}

	@Override
	public int hashCode() {
		return Objects.hash(comment, fecha, id, obra, texto, tipo, titulo, usuario, valoracion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Comentario other = (Comentario) obj;
		return Objects.equals(comment, other.comment) && Objects.equals(fecha, other.fecha)
				&& Objects.equals(id, other.id) && Objects.equals(obra, other.obra)
				&& Objects.equals(texto, other.texto) && Objects.equals(tipo, other.tipo)
				&& Objects.equals(titulo, other.titulo) && Objects.equals(usuario, other.usuario)
				&& valoracion == other.valoracion;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Comentario [id=").append(id).append(", titulo=").append(titulo).append(", texto=").append(texto)
				.append(", fecha=").append(fecha).append(", valoracion=").append(valoracion).append(", tipo=")
				.append(tipo).append(", obra=").append(obra).append(", usuario=").append(usuario).append(", comment=")
				.append(comment).append("]");
		return builder.toString();
	}


	
	
	
}
