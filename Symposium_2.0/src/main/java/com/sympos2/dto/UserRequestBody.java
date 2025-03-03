package com.sympos2.dto;

/**
 * This record shows Users data, specifically the parameters: email and password. 
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see Usuario
 */
public record UserRequestBody (String email, String password){
	
}
