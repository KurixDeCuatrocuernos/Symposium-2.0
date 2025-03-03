package com.sympos2.dto;

import com.sympos2.models.Comentario;

/**
 * This record shows Commentario and User object's data together, specifically, Comentario object and the parameters: userid, username and userrole. 
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see Comentario
 * @see Usuario
 */
public record ComentarioPintado (Comentario comment, String userid, String username, String userrole) {

}
