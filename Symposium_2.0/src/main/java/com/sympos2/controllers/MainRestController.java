package com.sympos2.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sympos2.dto.ComentarioValue;
import com.sympos2.dto.ObraIsbnTituloProjection;
import com.sympos2.dto.UserRequestBody;
import com.sympos2.dto.UsuarioComentarioPintado;
import com.sympos2.models.Comentario;
import com.sympos2.models.Obra;
import com.sympos2.models.Usuario;
import com.sympos2.repositories.ComentarioRepository;
import com.sympos2.repositories.ObraRepository;
import com.sympos2.repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class MainRestController {
	
	@Autowired
	private ObraRepository obraRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ComentarioRepository commentRepo;
	
	@Autowired
	private AuthenticationManager authManager;
	
	@Autowired
	private HttpServletRequest request;
	

	
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
	public ResponseEntity<String> getUserRole() {
		System.out.println("Enviando Role del usuario");
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	    if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
	        try {
	        	Optional<Usuario> user = userRepo.findByEmail(auth.getName());
	        	System.out.println("role: "+user.get().getRole());
	        	if (user.isPresent()) {
	        		ObjectMapper toJson = new ObjectMapper();
	        		String json = toJson.writeValueAsString(user.get().getRole());
		            System.out.println("Json recogido: " + json);
		            return ResponseEntity.ok(json);
	        	} else {
	        		return ResponseEntity.ok("{\"role\": null}"); 
	        	}
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("{\"error\": \"Error serializando el JSON\"}");
	        }
	    } else {
	        return ResponseEntity.ok("{\"role\": null}"); // JSON válido en vez de "null"
	    }
	}

	
	@GetMapping("/getUserAvatar")
	public ResponseEntity<String> getUserAvatar() {
	    System.out.println("Se ha buscado al usuario");
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	    if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
	        try {
	            System.out.println("Buscando al usuario en BD: " + auth.getName());
	            Optional<Usuario> user = userRepo.findByEmailOnlyId(auth.getName());

	            if (user.isPresent()) {
	                Optional<UsuarioComentarioPintado> userToSend = userRepo.findByIdOnlyIdAndNameAndRole(user.get().getId());

	                if (userToSend.isPresent()) {
	                    UsuarioComentarioPintado usuario = userToSend.get();
	                    
	                    // Crear JSON estructurado
	                    Map<String, String> responseData = new HashMap<>();
	                    responseData.put("avatar", usuario.avatar() != null ? usuario.avatar() : "./usuario.png");
	                    responseData.put("username", usuario.name() != null ? usuario.name() : "Usuario desconocido");

	                    // Serializar correctamente el objeto como JSON
	                    ObjectMapper toJson = new ObjectMapper();
	                    String json = toJson.writeValueAsString(responseData);
	                    
	                    return ResponseEntity.ok(json);
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.internalServerError().body("{\"error\": \"Error interno del servidor\"}");
	        }
	    }
	    
	    return ResponseEntity.badRequest().body("{\"error\": \"Usuario no autenticado\"}");
	}

	
	@PostMapping("/getLogin")
	public ResponseEntity<Map<String, String>> loginUser(@RequestBody UserRequestBody user) {
	    System.out.println("Tratando de iniciar sesión: " + user.email());
	    
	    if (user != null) {
	        try {
	            Optional<Usuario> userLogged = userRepo.findByEmail(user.email());
	            if (userLogged.isPresent()) {
	                UsernamePasswordAuthenticationToken authToken =
	                        new UsernamePasswordAuthenticationToken(userLogged.get().getEmail(), user.password());
	                System.out.println("Usuario encontrado en BD: " + userLogged.get().getEmail());
	                Authentication authentication = authManager.authenticate(authToken);
	                SecurityContextHolder.getContext().setAuthentication(authentication);

	                // Guardar en sesión HTTP
	                HttpSession session = request.getSession(true);
	                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

	                System.out.println("Sesión iniciada correctamente");
	                
	                // 🟢 Devolver un JSON válido
	                Map<String, String> response = new HashMap<>();
	                response.put("message", "Login successful");
	                return ResponseEntity.ok(response);
	            } else {
	                // 🔴 Devolver JSON de error
	                Map<String, String> response = new HashMap<>();
	                response.put("error", "User not found");
	                return ResponseEntity.badRequest().body(response);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            Map<String, String> response = new HashMap<>();
	            response.put("error", "Internal server error");
	            return ResponseEntity.internalServerError().body(response);
	        }
	    }
	    Map<String, String> response = new HashMap<>();
	    response.put("error", "Invalid request");
	    return ResponseEntity.badRequest().body(response);
	}

	
	@PostMapping("/getLogout")
	public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response) {
	    try {
	        // Invalidar la sesión
	        HttpSession session = request.getSession(false);
	        if (session != null) {
	            session.invalidate();
	        }

	        // Eliminar la autenticación del contexto de seguridad
	        SecurityContextHolder.clearContext();

	        return ResponseEntity.ok("Logout exitoso");
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.internalServerError().body("Error al cerrar sesión");
	    }
	}
	
	@GetMapping("/getWriting")
	public ResponseEntity<String> getWriting(@RequestParam Long id) {
		System.out.println("Buscando obra con isbn: "+id);
		ResponseEntity<String> retorno = ResponseEntity.ok(null);
		if(id!=null) {
			try {
				
				Optional<Obra> obra = obraRepo.findById(id);
				if(obra.isPresent()) {
					
					int valorMedia = getAVG(id);
					
					Map<String, String> responseData = new HashMap<>();
					responseData.put("titulo", obra.get().getTitulo());
                    responseData.put("autor", obra.get().getAutor());
                    responseData.put("fechaPub", obra.get().getFechaPublicacion().toString());
                    responseData.put("place", obra.get().getLugar_publicacion());
                    responseData.put("edit", obra.get().getEditorial());
                    responseData.put("type", obra.get().getTipo());
                    responseData.put("abstract", obra.get().getAbstracto());
                    responseData.put("valoracion", String.valueOf(valorMedia));
                    if(!obra.get().getTemas().isEmpty()) {
                    	responseData.put("temas", obra.get().getTemas().toString());
                    }
                    if(obra.get().getTipo().equals("ARTICLE")) {
                    	responseData.put("paginaIni", String.valueOf(obra.get().getPaginaini()));
                    	responseData.put("paginaFin", String.valueOf(obra.get().getPaginafin()));
                    
                    }
                    
                    ObjectMapper objectMapper = new ObjectMapper();
					String json = objectMapper.writeValueAsString(responseData);
					retorno = ResponseEntity.ok(json);
					
				} else {
					retorno=ResponseEntity.internalServerError().body("No se ha encontrado obra con esa id");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				retorno = ResponseEntity.internalServerError().body("Error al buscar la obra con esa id");
			}
		} else {
			retorno = ResponseEntity.internalServerError().body("No se ha recibido id para buscar");
		}
		
		return retorno;
	}
	
	private int getAVG(Long id) {
		int valor=0;
		
		List<Comentario> valores = commentRepo.findAllByObraOnlyComment(id);
		if(!valores.isEmpty()) {
			int suma=0;
			int contador=0;
			for (Comentario comment : valores) {
				suma = suma + comment.getValoracion();
				contador++;
			}
			valor=suma/contador;
			System.out.println("valores recogidos: "+valores.toString());
			System.out.println("media= "+valor);
		}
		
		return valor;
	}
	
}
