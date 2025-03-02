package com.sympos2.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sympos2.dto.ComentarioPintado;
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
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	
	@GetMapping("/getNewestWriting")
	public ResponseEntity<String> getNewestWriting() throws JsonProcessingException{
		 Map<String, Object> rs = new HashMap<>();
		 ObjectMapper om = new ObjectMapper();
		 om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		 om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		 try {
			 List<Obra> obras = obraRepo.findOneByOrderByCreatedAtDesc();
			 System.out.println(obras.get(0));
			 
			 if (!obras.isEmpty()) {
				 rs.put("status", "true");
				 rs.put("writing", obras.get(0));
			 }
		 } catch (Exception e) {
			 rs.put("status", "false");
			 rs.put("message", "There was a exception in server: "+e);
		 }
		 
		 String json= om.writeValueAsString(rs);
		 return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getMostValuedWriting")
	public ResponseEntity<String> getMostValuedWriting() throws JsonProcessingException{
		 Map<String, Object> rs = new HashMap<>();
		 ObjectMapper om = new ObjectMapper();
		 om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		 om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		 try {
			 List<Obra> obras = obraRepo.findAll();
			 if (!obras.isEmpty()) {
				 Obra MVObra = new Obra();
				 int value=0;
				 for (Obra obra : obras) {
					 if (value < getAVG(obra.getIsbn())) {
						 MVObra=obra;
					 }
				 }
				 
				 rs.put("status", "true");
				 rs.put("writing", MVObra);
			 }
		 } catch (Exception e) {
			 rs.put("status", "false");
			 rs.put("message", "There was a exception in server: "+e);
		 }
		 
		 String json= om.writeValueAsString(rs);
		 return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getTitledComment")
	public ResponseEntity<String> getTitledComment() throws JsonProcessingException{
		 Map<String, Object> rs = new HashMap<>();
		 ObjectMapper om = new ObjectMapper();
		 om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		 om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		 try {
			 
			 	List <Comentario> comments = commentRepo.findAllByTipo("COMMENT");
			 	List<Usuario> users = userRepo.findAllByRoleOnlyId("STUDENT");
		        // Crear un conjunto de IDs de usuarios con rol "STUDENT"
		        Set<String> studentIds = users.stream()
		                                      .map(Usuario::getId) // Obtener los IDs de los usuarios
		                                      .collect(Collectors.toSet()); // Guardarlos en un Set para una búsqueda eficiente

		        // Usamos un Iterator para eliminar los comentarios de estudiantes
		        Iterator<Comentario> iterator = comments.iterator();
		        while (iterator.hasNext()) {
		            Comentario comment = iterator.next();
		            if (studentIds.contains(comment.getUsuario())) {
		                iterator.remove(); // Elimina el comentario si su usuario es estudiante
		            }
		        }
			    Optional<Comentario> newestComment = comments.stream().max((c1, c2) -> c1.getFecha().compareTo(c2.getFecha()));
			 	Optional<UsuarioComentarioPintado> user = userRepo.findByIdOnlyIdAndNameAndRole(newestComment.get().getUsuario());
			 	ComentarioPintado commentToSend = new ComentarioPintado(newestComment.get(), user.get().id(), user.get().name(), user.get().role());
			 	rs.put("comment", commentToSend);
			 	rs.put("status", "true");
				
		 } catch (Exception e) {
			 rs.put("status", "false");
			 rs.put("message", "There was a exception in server: "+e);
		 }
		 
		 String json= om.writeValueAsString(rs);
		 return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getBanComment")
	public ResponseEntity<String> getBanComment(@RequestParam String id) throws JsonProcessingException {
		Map<String, Object> rs = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		
		if (id != null) {
			try {
				Optional<Comentario> comment = commentRepo.findById(id);
				if (comment.isPresent()) {
					if(comment.get().getTipo().equals("COMMENT")) {
						comment.get().setTipo("BANNED_COMMENT");
						commentRepo.save(comment.get());
						rs.put("status", "true");
					} else if (comment.get().getTipo().equals("ANSWER")) {
						comment.get().setTipo("BANNED_ANSWER");
						commentRepo.save(comment.get());
						rs.put("status", "true");
					} else {
						rs.put("status", "false");
						rs.put("message", "The comment to delete is not a Cmment nor an Answer");
					}
						
				} else {
					rs.put("status", "false");
					rs.put("message", "Couldn't find a comment with id: "+id);
				}
				
			} catch (Exception e) {
				rs.put("status", "false");
				rs.put("message", "There was a exception in server: "+e);
			}
		} else {
			rs.put("status", "false");
			rs.put("message", "Server didn't recieve Id");
		}
		
		
		String json= om.writeValueAsString(rs);
		return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getAllBanComments")
	public ResponseEntity<String> getAllBanComments() throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		try {
			List<Comentario> banComments = commentRepo.findAllByTipo("BANNED_COMMENT");
			List<Comentario> banAnswers = commentRepo.findAllByTipo("BANNED_ANSWER");
			List<ComentarioPintado> comments = new ArrayList<ComentarioPintado>();
			for (Comentario comment : banComments) {
				Optional<Usuario> user = userRepo.findById(comment.getUsuario());
				if (user.isPresent()) {
					comments.add(new ComentarioPintado(comment, user.get().getId(), user.get().getUsername(), user.get().getRole()));
				}
			}
			for (Comentario comment : banAnswers) {
				Optional<Usuario> user = userRepo.findById(comment.getUsuario());
				if (user.isPresent()) {
					comments.add(new ComentarioPintado(comment, user.get().getId(), user.get().getUsername(), user.get().getRole()));
				}
			}
			if (!comments.isEmpty()) {
				System.out.println("Banned Comments sended");
				rs.put("status", "true");
				rs.put("array", comments);
			} else {
				System.out.println("No Banned Comments found");
				rs.put("status", "true");
				rs.put("array", "false");
			}
		} catch (Exception e) {
			rs.put("status", "false");
			rs.put("message", "There was a exception in server: "+e);
		}
		
		String json= om.writeValueAsString(rs);
		return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getDelComment")
	public ResponseEntity<String> getDeleteComment(@RequestParam String id) throws JsonProcessingException {
		Map<String, Object> rs = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		
			if (id != null) {
				try {
					
					List<Comentario> answers = commentRepo.findAllByComment(id);
					if (!answers.isEmpty()) {
						for (Comentario answer : answers) {
							commentRepo.deleteById(answer.getId());
						}
					}
					commentRepo.deleteById(id);
					rs.put("status", "true");
					
				} catch (Exception e) {
					rs.put("status", "false");
					rs.put("message", "There was a exception in server: "+e);
				}
			} else {
				rs.put("status", "false");
				rs.put("message", "Server didn't recieve Id");
			}
		
		String json= om.writeValueAsString(rs);
		return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getUnbanComment")
	public ResponseEntity<String> getUnbanComment(@RequestParam String id) throws JsonProcessingException {
		Map<String, Object> rs = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		
			if (id != null) {
				try {
					Optional<Comentario> comment = commentRepo.findById(id);
					
					if (comment.isPresent()) {
						if(comment.get().getTipo().equals("BANNED_COMMENT")) {
								comment.get().setTipo("COMMENT");
								commentRepo.save(comment.get());
								rs.put("status", "true");
							} else if (comment.get().getTipo().equals("BANNED_ANSWER")) {
								comment.get().setTipo("ANSWER");
								commentRepo.save(comment.get());
								rs.put("status", "true");
							} else {
								rs.put("status", "false");
								rs.put("message", "The comment to unban was not a Comment nor an Answer");
							}


					} else {
						rs.put("status", "false");
						rs.put("message", "Couldn't find a comment with id: "+id);
					}

					
				} catch (Exception e) {
					rs.put("status", "false");
					rs.put("message", "There was a exception in server: "+e);
				}
			} else {
				rs.put("status", "false");
				rs.put("message", "Server didn't recieve Id");
			}
		
		String json= om.writeValueAsString(rs);
		return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getStudentComment")
	public ResponseEntity<String> getStudentComment() throws JsonProcessingException{
		 Map<String, Object> rs = new HashMap<>();
		 ObjectMapper om = new ObjectMapper();
		 om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		 om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		 try {
			 
			 	List <Comentario> comments = commentRepo.findAllByTipo("COMMENT");
			 	List<Usuario> users = userRepo.findAllByRoleOnlyId("TITLED");
		        // Crear un conjunto de IDs de usuarios con rol "STUDENT"
		        Set<String> studentIds = users.stream()
		                                      .map(Usuario::getId) // Obtener los IDs de los usuarios
		                                      .collect(Collectors.toSet()); // Guardarlos en un Set para una búsqueda eficiente

		        // Usamos un Iterator para eliminar los comentarios de estudiantes
		        Iterator<Comentario> iterator = comments.iterator();
		        while (iterator.hasNext()) {
		            Comentario comment = iterator.next();
		            if (studentIds.contains(comment.getUsuario())) {
		                iterator.remove(); // Elimina el comentario si su usuario es estudiante
		            }
		        }
			   
		        Optional<Comentario> newestComment = comments.stream().max((c1, c2) -> c1.getFecha().compareTo(c2.getFecha()));
			 	Optional<UsuarioComentarioPintado> user = userRepo.findByIdOnlyIdAndNameAndRole(newestComment.get().getUsuario());
			 	ComentarioPintado commentToSend = new ComentarioPintado(newestComment.get(), user.get().id(), user.get().name(), user.get().role());
			 	
			 	rs.put("comment", commentToSend);
			 	rs.put("status", "true");
				
		 } catch (Exception e) {
			 rs.put("status", "false");
			 rs.put("message", "There was a exception in server: "+e);
		 }
		 
		 String json= om.writeValueAsString(rs);
		 return ResponseEntity.ok(json);
	}
	
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
	        	
	        	if (user.isPresent()) {
	        		System.out.println("role: "+user.get().getRole());
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
	
	@GetMapping("/getUsername")
	public ResponseEntity<String> getUsername() throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
	    	
	    	Optional<Usuario> user = userRepo.findByEmail(auth.getName());
	    	if(user.isPresent()) {
	    		rs.put("status", "true");
	    		rs.put("user", user.get().getName());
	    	} else {
	    		rs.put("status", "true");
	    		rs.put("user", auth.getName());
	    	}
	    	
	    	
	    } else {
	    	rs.put("status", "true");
	    	rs.put("user", "false");
	    }
	    
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}

	
	@PostMapping("/getLogin")
	public ResponseEntity<String> loginUser(@RequestBody UserRequestBody user) throws JsonProcessingException {
	    System.out.println("Tratando de iniciar sesión: " + user.email());
	    Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    if (user != null) {
	        try {
	            Optional<Usuario> userLogged = userRepo.findByEmail(user.email());
	            if (userLogged.isPresent()) {
	            	try {
		            	UsernamePasswordAuthenticationToken authToken =
		                     new UsernamePasswordAuthenticationToken(userLogged.get().getEmail(), user.password());
		                System.out.println("Usuario encontrado en BD: " + userLogged.get().getEmail());
		                Authentication authentication = authManager.authenticate(authToken);
		                SecurityContextHolder.getContext().setAuthentication(authentication);
	
		                // Guardar en sesión HTTP
		                HttpSession session = request.getSession(true);
		                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
						rs.put("status", "true");
		                System.out.println("Sesión iniciada correctamente");
	            	} catch (BadCredentialsException excec) {
	            		rs.put("status", "false");
	 	                rs.put("message", "These credentials don't match, please check the password and email, and look caps and numbers");
	            	}
	               
	                

	                
	            } else {
	                rs.put("status", "false");
	                rs.put("message", "These credentials don't match, please check the password and email, and look caps and numbers");
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            rs.put("status", "false");
	            rs.put("message", "Internal server error, try it later or contact with an Admin");
	        }
	    }
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
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
	public ResponseEntity<String> getWriting(@RequestParam Long id) throws JsonProcessingException {
	    Map<String, String> response = new HashMap<>();
	    ResponseEntity<String> retorno = ResponseEntity.ok("Entered to the back, but not completed");

	    System.out.println("Buscando obra con isbn: " + id);

	    if (id != null) {
	        try {
	            Optional<Obra> obra = obraRepo.findById(id);
	            ObjectMapper objectMapper = new ObjectMapper();
	            if (obra.isPresent()) {
	                int valorMedia = getAVG(id);

//	                Map<String, String> data = new HashMap<>();
	                
	                response.put("titulo", obra.get().getTitulo());
	                response.put("autor", obra.get().getAutor());
	                response.put("fechaPub", obra.get().getFechaPublicacion().toString());
	                response.put("place", obra.get().getLugar_publicacion());
	                response.put("edit", obra.get().getEditorial());
	                response.put("type", obra.get().getTipo());
	                response.put("abstract", obra.get().getAbstracto());
	                response.put("valoracion", String.valueOf(valorMedia));
	                
	                if (!obra.get().getTemas().isEmpty()) {
	                    response.put("temas", obra.get().getTemas().toString());
	                }

	                if (obra.get().getTipo().equals("ARTICLE")) {
	                    response.put("paginaIni", String.valueOf(obra.get().getPaginaini()));
	                    response.put("paginaFin", String.valueOf(obra.get().getPaginafin()));
	                }

	                
//	                String json = objectMapper.writeValueAsString(data);
	                response.put("status", "true");
	                response.put("message", "Obra encontrada con éxito");
//	                response.put("data", json); // Añadir los datos en la respuesta
	                retorno = ResponseEntity.ok(objectMapper.writeValueAsString(response));
	            } else {
	                response.put("status", "false");
	                response.put("message", "No se ha encontrado obra con esa id");
	                retorno = ResponseEntity.ok(objectMapper.writeValueAsString(response));
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.put("status", "false");
	            response.put("message", "Error al buscar la obra con esa id");
	            ObjectMapper objectMapper = new ObjectMapper();
	            retorno = ResponseEntity.ok(objectMapper.writeValueAsString(response));
	        }
	    } else {
	        response.put("status", "false");
	        response.put("message", "No se ha recibido id para buscar");
	        ObjectMapper objectMapper = new ObjectMapper();
	        retorno = ResponseEntity.ok(objectMapper.writeValueAsString(response));
	    }

	    return retorno;
	}

	@GetMapping("/getComentarios")
	public ResponseEntity<String> getComentarios(@RequestParam Long id, @RequestParam String role) throws JsonProcessingException {
	    System.out.println("Recogiendo los comentarios de la obra");

	    Map<String, String> response = new HashMap<>();
	    ResponseEntity<String> answer = ResponseEntity.ok("Entered to the back, but not completed");

	    try {
	        Sort sortByDate = Sort.by(Order.desc("fecha"));
	        List<Comentario> comments = commentRepo.findAllByObraAndTipo(id, "COMMENT", sortByDate);
	        
	        if (!comments.isEmpty()) {
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	            List<Map<String, String>> list = new ArrayList<>();

	            for (Comentario comment : comments) {
	                Optional<UsuarioComentarioPintado> userData = userRepo.findByIdOnlyIdAndNameAndRole(comment.getUsuario());
	                
	                if (userData.isPresent() && userData.get().role().equals(role)) {
	                    Map<String, String> rd = new HashMap<>();
	                    rd.put("title", comment.getTitulo());
	                    rd.put("text", comment.getTexto());
	                    rd.put("id", comment.getId());
	                    rd.put("datetime", comment.getFecha().format(formatter));
	                    rd.put("value", String.valueOf(comment.getValoracion()));
	                    rd.put("username", userData.get().name());
	                    rd.put("school", userData.get().school());
	                    rd.put("role", userData.get().role());
	                    list.add(rd);
	                }
	            }
	            
	            // Creando la respuesta JSON
	            response.put("status", "true");
	            response.put("message", "Comentarios encontrados con éxito");
	            ObjectMapper objectMapper = new ObjectMapper();
	            String json = objectMapper.writeValueAsString(list);
	            
	            answer = ResponseEntity.ok(json);  // Enviamos los comentarios encontrados como JSON

	        } else {
	            response.put("status", "false");
	            response.put("message", "No se encontraron comentarios para esta obra.");
	            ObjectMapper objectMapper = new ObjectMapper();
	            String json = objectMapper.writeValueAsString(response);
	            
	            answer = ResponseEntity.ok(json);  // Respuesta de error si no se encuentran comentarios
	        }
	    } catch (Exception e) {
	        response.put("status", "false");
	        response.put("message", "Error al obtener los comentarios: " + e.getMessage());
	        ObjectMapper objectMapper = new ObjectMapper();
	        String json = objectMapper.writeValueAsString(response);
	        
	        answer = ResponseEntity.ok(json);  // En caso de excepción, respondemos con el error
	    }

	    return answer;
	}

	@GetMapping("/getAnswers")
	public ResponseEntity<String> getAnswers(@RequestParam Long id, @RequestParam String comment) throws JsonProcessingException {
	    Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    
	    try {
	        // Ordenamos por fecha descendente
	        Sort sortByDate = Sort.by(Order.desc("fecha"));
	        
	        // Obtenemos las respuestas asociadas al comentario y la obra
	        List<Comentario> answers = commentRepo.findAllByObraAndTipoAndComment(id, "ANSWER", comment, sortByDate);
	        
	        if (answers != null && !answers.isEmpty()) {
	            // Lista para almacenar las respuestas formateadas
	            List<Map<String, String>> list = new ArrayList<>();
	            
	            // Recorremos cada respuesta
	            for (Comentario answer : answers) {
	                // Obtenemos los datos del usuario que realizó la respuesta
	                Optional<UsuarioComentarioPintado> userData = userRepo.findByIdOnlyIdAndNameAndRole(answer.getUsuario());
	                
	                if (userData.isPresent()) {
	                    // Creamos un mapa para la respuesta con la información formateada
	                    Map<String, String> rd = new HashMap<>();
	                    rd.put("title", answer.getTitulo());
	                    rd.put("text", answer.getTexto());
	                    rd.put("id", answer.getId());
	                    rd.put("datetime", answer.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
	                    rd.put("value", String.valueOf(answer.getValoracion()));
	                    rd.put("username", userData.get().name());
	                    rd.put("school", userData.get().school());
	                    rd.put("role", userData.get().role());
	                    rd.put("comentario", answer.getComment());
	                    
	                    // Añadimos la respuesta formateada a la lista
	                    list.add(rd);
	                }
	            }
	           
	            rs.put("array", list);
	            System.out.println(list.toString());
	        } else {

	            rs.put("array", new ArrayList<>());
	        }
	        
	        // Añadimos la información del estado y el mensaje
	        rs.put("status", "true");
	        rs.put("message", "Comentarios encontrados con éxito");
	    } catch (Exception e) {
	        // Si ocurre un error, asignamos el estado "false"
	        rs.put("status", "false");
	        rs.put("message", "Error al obtener los comentarios: " + e.getMessage());
	    }

	    // Convertimos el mapa a un JSON
	    String json = om.writeValueAsString(rs);
	    
	    // Devolvemos la respuesta como JSON
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getCommented")
	public ResponseEntity<String> getCommented(Long id, String user) {
		System.out.println("Se va a buscar algún comentario del usuario: "+user+" en la obra : "+id);
		ResponseEntity<String> response = ResponseEntity.ok("{\"commented\": \"false\"}");
		Map <String, String> rd = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			List<Comentario> comments = commentRepo.findAllByObraAndUsuario(id, user);
			if (!comments.isEmpty()) {
				boolean commented = false;
				for (Comentario comment : comments) {
					if (comment.getTipo().equals("COMMENT")) {
						commented = true;
						System.out.println("Has comentado");
					}
				}
				rd.put("commented", String.valueOf(commented));
				String json = objectMapper.writeValueAsString(rd);
				response = ResponseEntity.ok(json);
			}
			
		} catch (Exception e) {
			response = ResponseEntity.internalServerError().body("Cannot get comments of user");
		}
		
		
		return response;
	}
	
	@GetMapping("/getUserIdent")
	public ResponseEntity<String> getUserId() throws JsonProcessingException {
	    Map<String, String> response = new HashMap<>();
	    ResponseEntity<String> answer = ResponseEntity.ok("Entered to the back, but not completed");
	    
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    
	    if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
	        response.put("status", "false");
	        response.put("message", "User not authenticated");
	        ObjectMapper objectMapper = new ObjectMapper();
	        String json = objectMapper.writeValueAsString(response);
	        answer = ResponseEntity.ok(json);
	    } else {
	        String id = null;
	        try {
	            System.out.println("Buscando al usuario con email: " + auth.getName());
	            Optional<Usuario> user = userRepo.findByEmailOnlyId(auth.getName());
	            if (user.isPresent()) {
	                id = user.get().getId();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.put("status", "false");
	            response.put("message", "Error fetching user");
	            ObjectMapper objectMapper = new ObjectMapper();
	            String json = objectMapper.writeValueAsString(response);
	            answer = ResponseEntity.ok(json);
	        }

	        if (id != null) {
	            Map<String, String> rd = new HashMap<>();
	            rd.put("status", "true");
	            rd.put("message", "User found");
	            rd.put("id", id);
	            
	            ObjectMapper objectMapper = new ObjectMapper();
	            String json = objectMapper.writeValueAsString(rd);
	            answer = ResponseEntity.ok(json);
	        } else {
	            response.put("status", "false");
	            response.put("message", "Cannot get User Identity");
	            ObjectMapper objectMapper = new ObjectMapper();
	            String json = objectMapper.writeValueAsString(response);
	            answer = ResponseEntity.ok(json);
	        }
	    }
	    
	    return answer;
	}

	
	@PostMapping("/postCommentInserted")
	public ResponseEntity<String> postCommentInserted(@RequestBody Comentario request) throws JsonProcessingException {
	    Map<String, String> response = new HashMap<>();
	    ResponseEntity<String> answer = ResponseEntity.ok("Entered to the back, but not completed");

	    // Validación de los campos del comentario
	    if (request.getTitulo() != null && !request.getTitulo().isEmpty() && 
	        request.getTexto() != null && !request.getTexto().isEmpty() &&  
	        request.getUsuario() != null && !request.getUsuario().isEmpty() && 
	        request.getObra() != null) {
	        
//	        System.out.println("titulo: " + request.getTitulo() + ", text: " + request.getTexto() +
//	                ", value: " + request.getValoracion() + " user: " + request.getUsuario() + " isbn: " + request.getObra());
	        	
	    	try {
	    		request.setFecha( LocalDateTime.now());
	    		request.setId(null);
	    		commentRepo.save(request); 
	    		response.put("status", "true");
	    		response.put("message", "Datos recibidos con éxito");
	    	} catch (Exception e) {
	    		response.put("status", "false");
		        response.put("message", "Error al enviar los datos a la base de datos");
	    	}
	    	
	       
	    } else {
//	        System.out.println("No se han podido recibir los datos: \ntitulo: " + request.getTitulo() + 
//	                           "\n text: " + request.getTexto() + "\n value: " + request.getValoracion() + 
//	                           "\n user: " + request.getUsuario() + "\n isbn: " + request.getObra());
	        
	        response.put("status", "false");
	        response.put("message", "Error al recibir los datos");
	    }
	    ObjectMapper om = new ObjectMapper();
	    String json= om.writeValueAsString(response);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getIdComment")
	public ResponseEntity<String> getIdComment(@RequestParam Long isbn, @RequestParam String usr) throws JsonProcessingException{
		ResponseEntity<String> response = ResponseEntity.ok(null);
		ObjectMapper om = new ObjectMapper();
		Map<String, String> answer = new HashMap<>();
		
		if (isbn != null && usr != null) {
			try {
				Optional<Comentario> comment = commentRepo.findByObraAndUsuario(isbn, usr);
				if (!comment.isEmpty()) {
					answer.put("status", "true");
					answer.put("idComment", comment.get().getId());
				} else {
					answer.put("status", "false");
					answer.put("message", "There is not comment from that user in this writing");
					
				}
			} catch (Exception e) {
				answer.put("status", "false");
				answer.put("message", "There was an error getting the comment's id: "+e);
			}
		}
		String json = om.writeValueAsString(answer);
		return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getCommentEdit")
	public ResponseEntity<String> getCommentEdit(@RequestParam String id) throws JsonProcessingException{
		ResponseEntity<String> response = ResponseEntity.ok(null);
		ObjectMapper om = new ObjectMapper();
		Map<String, String> answer = new HashMap<>();
		System.out.println("searching comment with id: "+id);
		if (id!=null) {
			try {
				Optional<Comentario> comment = commentRepo.findById(id);
				if (!comment.isEmpty()) {
					System.out.println("found the comment: "+comment.get().toString());
					answer.put("status", "true");
					answer.put("title", comment.get().getTitulo());
					answer.put("text", comment.get().getTexto());
					answer.put("value", String.valueOf(comment.get().getValoracion()));
				} else {
					answer.put("status", "false");
					answer.put("message", "Couldn't found a comment with the id: "+id);
				}
			} catch (Exception e) {
				answer.put("status", "false");
				answer.put("message", "There was an error getting the comment from the database");
			}
		} else {
			answer.put("status", "false");
			answer.put("message", "server not recieve id to search");
		}
		
		String json = om.writeValueAsString(answer);
		return ResponseEntity.ok(json);
	}
	
	@PostMapping("/postCommentEdited")
	public ResponseEntity<String> postCommentEdited(@RequestBody Comentario request) throws JsonProcessingException {
//	    System.out.println("Revisando datos del comentario");
	    Map<String, String> response = new HashMap<>();
	    ResponseEntity<String> answer = ResponseEntity.ok("Entered to the back, but not completed");

	    // Validación de los campos del comentario
	    if (request.getTitulo() != null && !request.getTitulo().isEmpty() && 
	        request.getTexto() != null && !request.getTexto().isEmpty() &&  
	        request.getUsuario() != null && !request.getUsuario().isEmpty() && 
	        request.getObra() != null) {
	        
//	        System.out.println("titulo: " + request.getTitulo() + ", text: " + request.getTexto() +
//	                ", value: " + request.getValoracion() + " user: " + request.getUsuario() + " isbn: " + request.getObra());
	        	
	    	try {
	    		
	    		Optional<Comentario> comment = commentRepo.findByObraAndUsuario(request.getObra(), request.getUsuario());
	    		if(!comment.isEmpty()) {
	    			request.setId(comment.get().getId());
	    			request.setFecha( LocalDateTime.now());
		    		request.setTipo(comment.get().getTipo());
		    		commentRepo.save(request); 
		    		response.put("status", "true");
		    		response.put("message", "Datos recibidos con éxito");
	    		} else {
	    			response.put("status", "false");
	    			response.put("message", "There wasn't a comment to edit in database");
	    		}
	    		
	    		
	    	} catch (Exception e) {
	    		response.put("status", "false");
		        response.put("message", "Error al enviar los datos a la base de datos");
	    	}
	    	
	       
	    } else {
//	        System.out.println("No se han podido recibir los datos: \ntitulo: " + request.getTitulo() + 
//	                           "\n text: " + request.getTexto() + "\n value: " + request.getValoracion() + 
//	                           "\n user: " + request.getUsuario() + "\n isbn: " + request.getObra());
	        
	        response.put("status", "false");
	        response.put("message", "Error al recibir los datos");
	    }
	    ObjectMapper om = new ObjectMapper();
	    String json= om.writeValueAsString(response);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getEmails")
	public ResponseEntity<String> getEmails(@RequestParam String email) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    if (email != null) {
	    	
	    	if(email.contains("@") && email.matches(".*\\.(com|es)$")) {
	    		try {
	    			List<Usuario> emails = userRepo.findAllEmailsOnly();
	    			if (!emails.isEmpty()) {
	    				/*Check emails and stops if finds any match*/
	    				boolean isPresent = emails.stream().anyMatch(user -> user.getEmail().equals(email));
	    				if(isPresent==true) {
	    					rs.put("status", "true");
	    					rs.put("checkEmail", "false");
	    					rs.put("resp", "This email is already registered, try to log-in");
	    				} else {
	    					rs.put("status", "true");
	    					rs.put("checkEmail", "true");
	    				}
	    			}
		    	} catch (Exception e) {
			    	rs.put("status", "false");
			    	rs.put("message", "Error searching emails in database");
		    	}
	    	} else {
	    		rs.put("status","true");
	    		rs.put("checkEmail", "false");
	    	}
	 
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "The email received is null: "+email);
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getEmailsEdit")
	public ResponseEntity<String> getEmailsEdit(@RequestParam String email, @RequestParam String currentEmail) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    if (email != null) {
	    	
	    	if(email.contains("@") && email.matches(".*\\.(com|es)$")) {
	    		try {
	    			List<Usuario> emails = userRepo.findAllEmailsOnly();
	    			
	    			if (!emails.isEmpty()) {
	    				emails.removeIf(user -> user.getEmail().equals(currentEmail));
	    				/*Check emails and stops if finds any match*/
	    				boolean isPresent = emails.stream().anyMatch(user -> user.getEmail().equals(email));
	    				if(isPresent==true) {
	    					rs.put("status", "true");
	    					rs.put("checkEmail", "false");
	    					rs.put("resp", "This email is already registered, try to log-in");
	    				} else {
	    					rs.put("status", "true");
	    					rs.put("checkEmail", "true");
	    				}
	    			}
		    	} catch (Exception e) {
			    	rs.put("status", "false");
			    	rs.put("message", "Error searching emails in database");
		    	}
	    	} else {
	    		rs.put("status","true");
	    		rs.put("checkEmail", "false");
	    	}
	 
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "The email received is null: "+email);
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getUserToEdit")
	public ResponseEntity<String> getUserToEdit(@RequestParam String id) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    try {
	    	Optional<Usuario> user = userRepo.findById(id);
	    	if(!user.isEmpty()) {
	    		if(user.get().getRole().equals("STUDENT")) {
		    		rs.put("status", "true");
		    		rs.put("name", user.get().getName());
		    		rs.put("email", user.get().getEmail());
		    		rs.put("fechaNac", user.get().getFechaNac().toString());
		    		rs.put("role", user.get().getRole());
		    		
		    		rs.put("studies", (user != null && user.get().getStudies() != null) ? user.get().getStudies() : "");
		    		rs.put("school", (user != null && user.get().getSchool() != null) ? user.get().getSchool() : "");

	    		} else if (user.get().getRole().equals("TITLED")) {
		    		rs.put("status", "true");
		    		rs.put("name", user.get().getName());
		    		rs.put("email", user.get().getEmail());
		    		rs.put("fechaNac", user.get().getFechaNac().toString());
		    		rs.put("role", user.get().getRole());
		    		
		    		rs.put("studiesTitle", (user != null && user.get().getStudies_title() != null) ? user.get().getStudies_title() : "");
		    		rs.put("studyPlace", (user != null && user.get().getStudy_place() != null) ? user.get().getStudy_place() : "");
		    		rs.put("titleDate", (user != null && user.get().getTitle_date() != null) ? user.get().getTitle_date().toString() : "");
	    		} else if (user.get().getRole().equals("ADMIN")) {
		    		rs.put("status", "true");
		    		rs.put("name", user.get().getName());
		    		rs.put("email", user.get().getEmail());
		    		rs.put("fechaNac", user.get().getFechaNac().toString());
		    		rs.put("role", user.get().getRole());
		    		
		    		rs.put("phone", (user != null && user.get().getPhone() != null) ? user.get().getPhone().toString() : "");
	    		} else {
	    			rs.put("status", "false");
	    			rs.put("message", "The user collected has not role");
	    		}
	    		
	    	} else {
	    		rs.put("status", "false");
	    		rs.put("message", "Couldn´t find the user with id: "+id);
	    	}
	    } catch (Exception e) {
	    	rs.put("status", "false");
	    	rs.put("message", "Exception in server getting the user to edit: "+e);
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@PostMapping("/postUserEdited")
	public ResponseEntity<String> postUserEdited(@RequestBody Usuario user) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    Usuario editedUser = new Usuario();
	    System.out.println("Editando: "+user.toString());
	    editedUser.setId(user.getId());
	    if (StringUtils.hasText(user.getPassword())) {
	    	editedUser.setPassword(encoder.encode(user.getPassword()));
	    	System.out.println("Se modificó la contraseña");
	    }
	    if(StringUtils.hasText(user.getName())) {
	    	editedUser.setName(user.getName());
//	    	System.out.println("Se modificó el nombre");
	    }
	    if(StringUtils.hasText(user.getRole())) {
	    	editedUser.setRole(user.getRole());
	    	System.out.println("Se modificó el role");
	    }
	    if(StringUtils.hasText(user.getFechaNac().toString())) {
	    	editedUser.setFechaNac(user.getFechaNac());
//	    	System.out.println("Se modificó la fecha de nacimiento");
	    }
	    if(StringUtils.hasText(user.getEmail())) {
	    	editedUser.setEmail(user.getEmail());
	    	System.out.println("Se modificó el email");
	    }
	    if(StringUtils.hasText(user.getStudies())) {
	    	editedUser.setStudies(user.getStudies());
//	    	System.out.println("Se modificaron los estudios");
	    }
	    if(StringUtils.hasText(user.getSchool())) {
	    	editedUser.setSchool(user.getSchool());
//	    	System.out.println("Se modificó la escuela");
	    }
	    if(StringUtils.hasText(user.getStudies_title())) {
	    	editedUser.setStudies_title(user.getStudies_title());
//	    	System.out.println("Se modificaron los tirulos de estudio");
	    }
	    if(StringUtils.hasText(user.getStudy_place())) {
	    	editedUser.setStudy_place(user.getStudy_place());
//	    	System.out.println("Se modificó el lugar de estudio");
	    }
	    if(user.getTitle_date()!=null) {
	    	if(StringUtils.hasText(user.getTitle_date().toString())) {
	    		editedUser.setTitle_date(user.getTitle_date());
//	    		System.out.println("Se modificó la fecha del titulo");
	    	}
	    }
	    
	    if(user.getPhone()!=null) {
		    if(StringUtils.hasText(user.getPhone().toString())) {
		    	editedUser.setPhone(user.getPhone());
//		    	System.out.println("Se modificó el teléfono");
		    }
	    }
	    
	    try {
	    	userRepo.save(editedUser);
	    	rs.put("status", "true");
	    } catch(Exception e) {
	    	rs.put("status", "false");
	    	rs.put("message", "There was an error editing the user in server: "+e);
	    }
	     
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@PostMapping("/postRegistryUser")
	public ResponseEntity<String> postRegistryUser(@RequestBody Usuario user) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            rs.put("status", "false");
            rs.put("message", "You are already logged!");
        } else {
        	if(user!=null && user.getName()!=null && user.getEmail()!=null && user.getPassword()!=null && user.getFechaNac()!=null && user.getSchool()!=null && user.getStudies()!=null) {
        		user.setId(null);
        		user.setPassword(encoder.encode(user.getPassword()));
        		user.setRole("STUDENT");
        		
        		try {
        			userRepo.save(user);
        			rs.put("status", "true");
        		} catch (Exception e) {
                    rs.put("status", "false");
                    rs.put("message", "Couldn't insert the new user in database");
        		}
        	} else {
                rs.put("status", "false");
                rs.put("message", "Server didn't recieve an user to log");
        	}
        }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getAllIdUsers")
	public ResponseEntity<String> getAllIdUsers() throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    // those are modifications for ObjectMapper to allow Jackson library to read DataTime objects, first allow to read and the second formats it to have - between each value (Year-Month-Day)
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    try {
	    	List<Usuario> users = userRepo.findAll();
	    	if (!users.isEmpty()) {
//	    		List<String> ids = new ArrayList<String>();
//	    		for (Usuario user : users) {
//	    			ids.add(user.getId());
//	    		}
//	    		if (!ids.isEmpty()) {
	    			System.out.println(users.toString());
	    			rs.put("status", "true");
	    			rs.put("array", users);
//	    		} else {
//	    			rs.put("status", "false");
//			    	rs.put("message", "The List collected from the server is empty, this might be an error");
//	    		}
	    		
	    	} else {
	    		rs.put("status", "false");
		    	rs.put("message", "There are no users in database, this might be an error");
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	rs.put("status", "false");
	    	rs.put("message", "There was an error in server trying to fetch the users");
	    }
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getAllIdWorks")
	public ResponseEntity<String> getAllIdWorks() throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    // those are modifications for ObjectMapper to allow Jackson library to read DataTime objects, first allow to read and the second formats it to have - between each value (Year-Month-Day)
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    try {
	    	List<Obra> works = obraRepo.findAll();
	    	if (!works.isEmpty()) {
	    		
	    		System.out.println("writings to show: "+works.toString());
	    		
	    		System.out.println(works.toString());
	    		rs.put("status", "true");
	    		rs.put("array", works);
	    		
	    	} else {
	    		rs.put("status", "false");
		    	rs.put("message", "There are no Writings in database, this might be an error");
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	rs.put("status", "false");
	    	rs.put("message", "There was an error in server trying to fetch the writings");
	    }
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getUserDeleted")
	public ResponseEntity<String> getDeletedUser(@RequestParam String id) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    if (id!=null) {
	    	try {
	    		
	    		List<Comentario> comments = commentRepo.findAllByUsuario(id);
	    		for (Comentario comment : comments) {
	    			commentRepo.deleteById(comment.getId());
	    		}
	    		userRepo.deleteById(id);
	    		rs.put("status", "true");
		    } catch (Exception e) {
		    	rs.put("status", "false");
		    	rs.put("message", "Error connecting with server, deleting the user aborted");
		    }
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "The id recieved to delete is empty");
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getWorkDeleted")
	public ResponseEntity<String> getWorkDeleted(@RequestParam Long id) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    if (id!=null) {
	    	try {
	    		
	    		List<Comentario> comments = commentRepo.findAllByObra(id);
	    		for (Comentario comment : comments) {
	    			commentRepo.deleteById(comment.getId());
	    		}
	    		obraRepo.deleteById(id);
	    		rs.put("status", "true");
		    } catch (Exception e) {
		    	rs.put("status", "false");
		    	rs.put("message", "Error connecting with server, deleting the writing aborted");
		    }
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "The id recieved to delete is empty");
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getWorkToEdit")
	public ResponseEntity<String> getWorkToEdit(@RequestParam Long id) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    try {
	    	Optional<Obra> obra = obraRepo.findById(id);
	    	if(!obra.isEmpty()) {
	    		if(obra.get().getTipo().equals("BOOK")) {
		    		rs.put("status", "true");
		    		rs.put("title", obra.get().getTitulo());
		    		rs.put("autor", obra.get().getAutor());
		    		rs.put("publicationDate", obra.get().getFechaPublicacion().toString());
		    		rs.put("type", obra.get().getTipo());
		    		rs.put("abstract", obra.get().getAbstracto());
		    		rs.put("editorial", obra.get().getEditorial());
		    		rs.put("publicationPlace", obra.get().getLugar_publicacion());
		    		rs.put("temas", om.writeValueAsString(obra.get().getTemas()));

	    		} else if (obra.get().getTipo().equals("ARTICLE")) {
		    		rs.put("status", "true");
		    		rs.put("title", obra.get().getTitulo());
		    		rs.put("autor", obra.get().getAutor());
		    		rs.put("publicationDate", obra.get().getFechaPublicacion().toString());
		    		rs.put("type", obra.get().getTipo());
		    		rs.put("abstract", obra.get().getAbstracto());
		    		rs.put("editorial", obra.get().getEditorial());
		    		rs.put("publicationPlace", obra.get().getLugar_publicacion());
		    		rs.put("temas", om.writeValueAsString(obra.get().getTemas()));
		    		rs.put("PageIni", String.valueOf(obra.get().getPaginaini()));
		    		rs.put("PageFin", String.valueOf(obra.get().getPaginafin()));
	    		} else {
	    			rs.put("status", "false");
	    			rs.put("message", "The Writing collected has not type");
	    		}
	    		
	    	} else {
	    		rs.put("status", "false");
	    		rs.put("message", "Couldn´t find the Writing with isbn: "+id);
	    	}
	    } catch (Exception e) {
	    	rs.put("status", "false");
	    	rs.put("message", "Exception in server getting the writing to edit: "+e);
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@PostMapping("/postWorkEdited")
	public ResponseEntity<String> postWorkEdited(@RequestBody Obra work) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    System.out.println("Editing Work...: "+work.toString());
	    boolean cell = true;
	    String fields = "";
	    if (work.getIsbn()==null) {
	    	cell=false;
	    	fields="ISBN is empty\n";
	    }
	    if(work.getTitulo()==null || work.getTitulo()=="") {
	    	cell=false;
	    	fields+="Title is empty\n";
	    }
	    if (work.getAutor()==null || work.getAutor()=="") {
	    	cell=false;
	    	fields+="Autor is empty\n";

	    }
	    if (work.getEditorial()==null || work.getEditorial()=="") {
	    	cell=false;
	    	fields+="Editorial is empty\n";
	    }
	    if (work.getFechaPublicacion()==null) {
	    	cell=false;
	    	fields+="FechaPublicacion is empty\n";	    
	    }
	    if (work.getLugar_publicacion()==null||work.getLugar_publicacion()=="") {
	    	cell=false;
	    	fields+="Lugar_publicacion is empty\n";
	    }
	    if (work.getTipo()==null||work.getTipo()=="") {
	    	cell=false;
	    	fields+="Tipo is empty\n";
	    }
	    if (!work.getTipo().equals("BOOK") && !work.getTipo().equals("ARTICLE")) {
	    	cell=false;
	    	fields+="Tipo not equals BOOK nor ARTICLE\n";
	    }
	    if (work.getAbstracto()==null || work.getAbstracto()=="") {
	    	cell=false;
	    	fields+="Astracto is empty\n";
	    }
	    if (work.getTemas()==null || work.getTemas().isEmpty()) {
	    	cell=false;
	    	fields+="Temas is empty\n";
	    }
	    if (cell==true) {
	    	try {
		    	obraRepo.save(work);
		    	rs.put("status", "true");
		    } catch(Exception e) {
		    	rs.put("status", "false");
		    	rs.put("message", "There was an error editing the user in server: "+e);
		    }
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "Any writing's field is empty: "+fields);
	    }
	    
	     
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("getSearchWorkList")
	public ResponseEntity<String> getSearchWorkList(@RequestParam String search) throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    // those are modifications for ObjectMapper to allow Jackson library to read DataTime objects, first allow to read and the second formats it to have - between each value (Year-Month-Day)
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    try {
	    	List<Obra> works = obraRepo.findAllParams(search);
	    
	    	System.out.println("writings to show: "+works.toString());
	    		
	    	System.out.println(works.toString());
	    	rs.put("status", "true");
	    	rs.put("array", works);
	    		
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	rs.put("status", "false");
	    	rs.put("message", "There was an error in server trying to fetch the writings");
	    }
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getSearchUsersList")
	public ResponseEntity<String> getSearchUsersList(@RequestParam String search) throws JsonProcessingException {
	    Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    
	    try {
	        List<Usuario> users = userRepo.findAllParams(search); 

	        rs.put("status", "true");
	        rs.put("array", users);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        rs.put("status", "false");
	        rs.put("message", "There was an error in server trying to fetch the users");
	    }
	    
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/getSearchCommentList")
	public ResponseEntity<String> getSearchCommentList(@RequestParam String search) throws JsonProcessingException {
	    Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    
	    search = search.replaceAll("[^a-zA-Z0-9]", "");
	    try {
	        List<Comentario> commentsData = commentRepo.findAllParams(search); 
	        List<ComentarioPintado> comments = new ArrayList<ComentarioPintado>();
	        if (!commentsData.isEmpty()) {
	        	for(Comentario comment : commentsData) {
	        		if (comment.getTipo().equals("BANNED_COMMENT") || comment.getTipo().equals("BANNED_ANSWER")) {

		        		Optional<Usuario> user = userRepo.findById(comment.getUsuario());
		        		if (user.isPresent()) {

		        			comments.add(new ComentarioPintado(comment, user.get().getId(), user.get().getUsername(), user.get().getRole()));
		        		}
		        		
	        		}
	        	}
	        }
	        if (!comments.isEmpty()) {
	        	System.out.println("Found Comments with that search");
	        	rs.put("status", "true");
	        	rs.put("array", comments);
	        } else {
	        	System.out.println("Not found comments with that search");
	        	rs.put("status", "true");
	        	rs.put("array", comments);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        rs.put("status", "false");
	        rs.put("message", "There was an error in server trying to fetch the users");
	    }
	    
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@GetMapping("/geIsbnChecked")
	public ResponseEntity<String> getIsbnChecked(@RequestParam Long id) throws JsonProcessingException {
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    try {
	    	List<Obra> obras = obraRepo.findAllOnlyIsbn();
	    	if (obras.isEmpty()) {
	    		rs.put("status", "false");
	    		rs.put("message", "Error, no any ISBN received from database");
	    	} else {

	    		boolean isPresent = obras.stream().anyMatch(obra -> obra.getIsbn().equals(id));

	    		rs.put("status", "true");
	    		if (isPresent) {
	    			rs.put("present", "true");
	    		} else {
	    			rs.put("present", "false");
	    		}
	    		
	    	}

	    } catch (Exception e) {
	    	rs.put("status", "false");
	    	rs.put("message", "there was a problem in server: "+e);
	    }
		
		String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	@PostMapping("/postWorkInsert")
	public ResponseEntity<String> postWorkInsert(@RequestBody Obra work) throws JsonProcessingException {
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    System.out.println("obra a insertar: "+work);
	    
	    if (work.getIsbn()!=null && 
	    	work.getTitulo()!=null && work.getTitulo()!="" &&
	    	work.getAutor()!=null && work.getAutor()!="" &&
	    	work.getAbstracto()!=null && work.getAbstracto()!="" &&
	    	work.getEditorial()!=null && work.getEditorial()!="" &&
	    	work.getLugar_publicacion()!=null && work.getLugar_publicacion()!="" &&
	    	work.getTipo()!=null && work.getTipo()!="" &&
	    	work.getFechaPublicacion()!=null) {
	    	
	    	try {
	    		obraRepo.save(work);
	    		rs.put("status", "true");
	    	} catch (Exception e) {
	    		rs.put("status", "false");
		    	rs.put("message", "There was a problem connecting with Database: "+e);
	    	}
	    	
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "Any Writing field was empty, couldn't insert the writing in Database");
	    }
		
		String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
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
		}
		
		return valor;
	}
	
}
