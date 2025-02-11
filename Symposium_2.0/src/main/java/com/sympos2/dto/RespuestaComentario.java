package com.sympos2.dto;

import java.time.LocalDateTime;

public record RespuestaComentario (String id, String texto, LocalDateTime fecha, String tipo, String usuario, String comment) {

}
