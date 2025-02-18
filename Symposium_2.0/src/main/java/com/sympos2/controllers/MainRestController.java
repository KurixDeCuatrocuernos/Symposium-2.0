package com.sympos2.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sympos2.dto.ObraIsbnTituloProjection;
import com.sympos2.dto.UsuarioComentarioPintado;
import com.sympos2.models.Usuario;
import com.sympos2.repositories.ObraRepository;
import com.sympos2.repositories.UserRepository;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class MainRestController {
	
	@Autowired
	private ObraRepository obraRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	private ObjectMapper toJson;
	
	@GetMapping("/sugerencias")
    public List<ObraIsbnTituloProjection> suggestList(@RequestParam(required = false) String searchTerm) {
        List<ObraIsbnTituloProjection> retorno = new ArrayList<ObraIsbnTituloProjection>();
        List<ObraIsbnTituloProjection> suggestions = obraRepo.findAllIsbnAndTitulo();
        
        if (!suggestions.isEmpty()) {
        	
            retorno = suggestions;
            
        } else {
            System.out.println("No se pudo recoger las sugerencias");
        }
        return retorno;
    }
	
	@GetMapping("/getUserRole")
	public String getUserRole() {
		String retorno ="";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
			try {
				String json = toJson.writeValueAsString(auth.getPrincipal());
				System.out.println("Json recogido:"+json);
				retorno = json;
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			retorno="null";
		}
		return retorno;
	}
	
	@GetMapping("/getUserAvatar")
	public String getUserAvatar() {
		String retorno = "";
		System.out.println("Se ha buscado al usuario");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
			try {
				System.out.println("Buscando al usuario en BD");
				Optional<Usuario> user = userRepo.findByEmailOnlyId(auth.getName());
				if(!user.isEmpty()) {
					Optional<UsuarioComentarioPintado> userToSend = userRepo.findByIdOnlyIdAndNameAndRole(user.get().getId());
					if(userToSend.isPresent()) {
						retorno = userToSend.toString();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			retorno = "null";
		}
		return retorno;
	}
	
}
