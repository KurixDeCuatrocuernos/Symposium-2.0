package com.sympos2.dto;

import com.sympos2.models.Comentario;

public record ComentarioPintado (Comentario comment, String userid, String username, String userrole) {

}
