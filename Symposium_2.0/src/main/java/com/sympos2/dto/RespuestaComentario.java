package com.sympos2.dto;

import java.time.LocalDateTime;

/**
 * This record shows Commentario object's data, specifically the parameters: id, texto, fecha, tipo and comment. 
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see Comentario
 */
public record RespuestaComentario (String id, String texto, LocalDateTime fecha, String tipo, String usuario, String comment) {

}
