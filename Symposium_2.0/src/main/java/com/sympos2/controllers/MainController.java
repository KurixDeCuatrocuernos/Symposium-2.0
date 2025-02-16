package com.sympos2.controllers;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sympos2.dto.ComentarioPintado;
import com.sympos2.dto.ObraIsbnTituloProjection;
import com.sympos2.dto.UsuarioComentarioPintado;
import com.sympos2.models.Comentario;
import com.sympos2.models.Obra;
import com.sympos2.models.Usuario;
import com.sympos2.repositories.ComentarioRepository;
import com.sympos2.repositories.ObraRepository;
import com.sympos2.repositories.UserRepository;
import com.sympos2.services.ComentarioService;
import com.sympos2.services.ObraService;
import com.sympos2.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class MainController {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ObraRepository obraRepo;
	
	@Autowired
	private ComentarioRepository commentRepo;
	
	@Autowired
	private ObraService obraService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ComentarioService commentService;
	
	@Autowired
	private AuthenticationManager authManager;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@Autowired
	private HttpServletRequest request;  // Spring inyecta el HttpServletRequest automáticamente

	
//	@Autowired
//	private StorageService storage;
	
	@GetMapping("/")
	public String index(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println("Estoy en la página principal");
		if (auth != null && auth.isAuthenticated()) {
			model.addAttribute("username", auth.getName());
			model.addAttribute("userId", auth.getPrincipal());
		} else {
			model.addAttribute("username", "invitado");
		}
		List<ObraIsbnTituloProjection> suggestions = obraRepo.findAllIsbnAndTitulo();
		System.out.println("sugerencias: "+suggestions.toString());
		
		model.addAttribute("suggestWorks",suggestions);
		
		return "/index";
	}
	
	@GetMapping("/workShow")
	public String mostrarObra(@RequestParam() Long id, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Usuario user = new Usuario();
		if (auth != null && auth.isAuthenticated()) {
			model.addAttribute("username", auth.getName());
			if(auth.getName().equals("anonymousUser")) {
//				System.out.println("Usuario anónimo, credenciales predeterminados");
				model.addAttribute("user", user);
			} else {
//				System.out.println("usuario identificado");
				user = (Usuario) auth.getPrincipal();
				model.addAttribute("user", auth.getPrincipal());
			}
			
		} else {
			user = new Usuario();
			user.setId("invitado");
			model.addAttribute("username", "invitado");
			model.addAttribute("user", user);
		}
		String retorno = "";
		if (id != null) {
			Optional<Obra> obra = obraRepo.findById(id);
			if (obra != null && !obra.isEmpty()) {
				List<ObraIsbnTituloProjection> suggestions = obraRepo.findAllIsbnAndTitulo();
//				System.out.println("sugerencias: "+suggestions.toString());
				model.addAttribute("suggestWorks",suggestions);
				
//				System.out.println("mostrando obra: "+obra.get().toString());
				model.addAttribute("obra", obra.get());
				
				Sort sortByDate = Sort.by(Order.desc("fecha"));
				
				List<Comentario> comments= commentRepo.findAllByObraAndTipo(obra.get().getIsbn(), "COMMENT", sortByDate);
				model.addAttribute("comments", comments);
				
				List<ComentarioPintado> paintcomments = new ArrayList<ComentarioPintado>();
				Optional<UsuarioComentarioPintado> userToPaint;
				if (!comments.isEmpty()) {
					for(Comentario comment : comments) {
						
						userToPaint = userRepo.findByIdOnlyIdAndNameAndRole(comment.getUsuario());
						
						if(userToPaint.isPresent()) {
							paintcomments.add(new ComentarioPintado(comment,userToPaint.get().id(), userToPaint.get().name(), userToPaint.get().role()));
						}
					}
//					System.out.println("Comentarios enviados al modelo: ");
//					paintcomments.forEach(System.out::println);
					model.addAttribute("comments", paintcomments);
				} else {
					System.out.println("No se encontraron comentarios para esa obra");
				}
				
				sortByDate = Sort.by(Order.asc("fecha"));
				List<ComentarioPintado> paintanswers = new ArrayList<ComentarioPintado>();
				List<Comentario> answers = commentRepo.findAllByObraAndTipo(obra.get().getIsbn(), "ANSWER", sortByDate);
				if (!answers.isEmpty()) {
					for(Comentario answer : answers) {
						userToPaint= userRepo.findByIdOnlyIdAndNameAndRole(answer.getUsuario());
						
						if(userToPaint.isPresent()) {
							paintanswers.add(new ComentarioPintado(answer, userToPaint.get().id(), userToPaint.get().name(), userToPaint.get().role()));
						}
					}
//					System.out.println("Respuestas enviadas al modelo: ");
//					paintanswers.forEach(System.out::println);
					model.addAttribute("answers", paintanswers);
				} else {
					System.out.println("No se encontraron respuestas para esa obra");
				}
				
				boolean comentado=false;
				for(Comentario comment : comments) {
//					System.out.println("Comparando el idUsuario del comentario: "+comment.getUsuario()+" con el id del usuario: "+user.getId());
					if (comment.getUsuario().equals(user.getId())) {
						comentado=true;
					}
				}
				model.addAttribute("comentar", comentado);
				
				
				retorno="workShow";		
			} else {
				System.out.println("No se ha encontrado la obra con isbn: "+id);
				retorno="/";
			}
		} else {
			System.out.println("No se ha recibido id");
			retorno="/";
		}
		
		
		return retorno;
	}
	
	@PostMapping("/workComment/submit")
	public ResponseEntity<String> comentarObra(@RequestParam Long obraId, @RequestParam String userId, @RequestParam String titulo, @RequestParam String texto, @RequestParam int valoracion) {

	    String retorno = "";
	    try {
	    	
	        Comentario comment = new Comentario(null, titulo, texto, LocalDateTime.now(), valoracion, "COMMENT", obraId, userId);
	        commentRepo.save(comment);

	        // en este caso no recargamos la página para poder hacerlo en ella y mandar una confirmación
	        retorno = "/workShow?id=" + obraId;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar el comentario.");
	    }

	    return ResponseEntity.ok(retorno);
	}
	
	@GetMapping("/workShow/deleteComment")
	public String borrarComentario(@RequestParam String id, @RequestParam String obraId) {
		String retorno ="redirect:/workShow?id="+obraId;
		
		try {
			List<Comentario> answers = commentRepo.findAllByComment(id);
			if(!answers.isEmpty()) {
				System.out.println("Respuestas asociadas borradas");
				commentRepo.deleteAll(answers);
			} else {
				System.out.println("No se encontraron respuestas asociadas que borrar");
			}
			commentRepo.deleteById(id);
			
		} catch (Exception e) {
			e.printStackTrace();
			retorno="/errorPage";
		}
		
		return retorno;
	}
	
	@PostMapping("/answerComment/submit")
	public ResponseEntity<String> responderComentario(@RequestParam Long obraId, @RequestParam String userId, @RequestParam String commentId, @RequestParam String texto) {
		String retorno = "/workShow?id="+obraId;
		System.out.println("obra: "+obraId+" userId: "+userId+" commentId: "+" texto: "+texto);
		
		try {
			/*String id, String texto, LocalDateTime fecha, String tipo, Long obra,
			String usuario, String comment)*/
			Comentario comment = new Comentario (null, texto, LocalDateTime.now(), "ANSWER", obraId, userId, commentId);
			commentRepo.save(comment);
		} catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar el comentario.");
		}
		
		
		return ResponseEntity.ok(retorno);
		
	}
	
	@GetMapping("/editComment")
	@ResponseBody
	public ResponseEntity<Comentario> editarComentario(@RequestParam Long obraId, @RequestParam String commentId) {
	    Optional<Comentario> comment;
	    try {
	        comment = commentRepo.findById(commentId);
	        if (comment.isPresent()) {
	            return ResponseEntity.ok(comment.get()); // Devuelve el comentario como JSON
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Si no se encuentra el comentario
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Si hay un error
	    }
	}
	
	@PostMapping("/updateComment")
	@ResponseBody
	public ResponseEntity<String> updateComment(@RequestParam String commentId, @RequestParam String titulo, @RequestParam String texto, @RequestParam int valoracion) {
	    ResponseEntity<String> retorno = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el comentario");
		try {
	    	System.out.println("actualizando comentario");
	        Optional<Comentario> oldComment = commentRepo.findById(commentId);
	        if (oldComment.isPresent()) {
	        	Comentario newComment = oldComment.get();
	        	try {
	        		newComment.setTitulo(titulo);
	        		newComment.setTexto(texto);
	        		newComment.setValoracion(valoracion);
	        		commentRepo.save(newComment); 
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	} 
	            retorno = ResponseEntity.ok("Comentario actualizado");
	        } else {
	        	System.out.println("no se puede actualizar el comentario");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        retorno = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el comentario");
	    }
		return retorno;
	}
		
	@GetMapping("/login")
	public String login(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String retorno ="";
		if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			retorno= "redirect:/";
		} else {
			retorno = "/loginPage";
		}
		return retorno;
	}
	
	@PostMapping("/loginPage/submit")
	public String loginSubmit(@Valid @ModelAttribute Usuario logUser, Model model, RedirectAttributes redirectAttributes) {
		String retorno ="redirect:/";
		try {
            System.out.println("Intentando autenticar...");
			UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(logUser.getEmail(), logUser.getPassword());
			System.out.println("Usuario insertado en BD: "+logUser.getEmail()+" "+logUser.getPassword());
            Authentication authentication = authManager.authenticate(authToken);
            // Establecer la autenticación en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Autenticado");
            
            // Asegurarse de que la sesión HTTP almacena la autenticación
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Hubo error al autenticar");
            redirectAttributes.addFlashAttribute("message", "Registration successful, but login failed.");
            retorno = "redirect:/loginError";
        }
		return retorno;
	}
	
	@GetMapping("/loginError")
	public String loginError(Model modelo) {
		// cosas
		return "/loginError";
	}
	
	// mappings for user
	@GetMapping("/usersList")
	public String listado(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println("Estoy en la página principal");
		if (auth != null && auth.isAuthenticated()) {
			model.addAttribute("username", auth.getName());
		} else {
			model.addAttribute("username", "invitado");
		}
		String retorno = "";
		Usuario usuario = getCurrentUsuario();
		if (usuario != null && "ADMIN".equals(usuario.getRole())) {
			retorno = "/usersList";
			model.addAttribute("listaUsuarios", userRepo.findAll());
		} else {
			System.out.println("El usuario no es administrador");
			retorno = "/";
		}
		return retorno;
	}
	
	@GetMapping("form")
	public String registro(Model model) {
		model.addAttribute("usuarioEdit", null);
		return "/form";
	}
	
	@PostMapping("/form/submit")
	public String handleFormSubmit(@Valid @ModelAttribute("userForm") Usuario nuevoUsuario, Model model, BindingResult br, RedirectAttributes redirectAttributes) {
		String retorno="";
		String mensaje="";
		boolean check=true;
		
		if (br.hasErrors()) {
			br.getAllErrors().forEach(error -> System.out.println(error.toString()));
			check=false;
			mensaje+="Errors in register\n";
		} 
		if (nuevoUsuario.getName()==null) {
			check=false;
			mensaje+="You need an username";
		}
		if (nuevoUsuario.getFechaNac()==null) {
			check=false;
			mensaje+="You need a birthday date\n";
		}
		if (nuevoUsuario.getEmail()==null || !nuevoUsuario.getEmail().contains("@")) {
			check=false;
			mensaje+="You need a valid email\n";
		}
		
		if (check==false) {
			redirectAttributes.addFlashAttribute("message", mensaje);  // Usar addFlashAttribute
			retorno = "redirect:/form";  // Redirigir a la página de registro
		} else {
			nuevoUsuario.setRole("STUDENT");
			String rawPassword = nuevoUsuario.getPassword();
			nuevoUsuario.setPassword(encoder.encode(rawPassword));
			userRepo.save(nuevoUsuario);
			System.out.println(nuevoUsuario.getEmail());
			
			try {
	            System.out.println("Intentando autenticar...");
				UsernamePasswordAuthenticationToken authToken = 
	                new UsernamePasswordAuthenticationToken(nuevoUsuario.getEmail(), rawPassword);
				System.out.println("Usuario insertado en BD: "+nuevoUsuario.getEmail()+" "+nuevoUsuario.getPassword());
	            System.out.println("Usuario para inicio de sesión: "+authToken.getName()+" "+authToken.getCredentials()+" "+authToken.getDetails());
	            Authentication authentication = authManager.authenticate(authToken);
	            // Establecer la autenticación en el contexto de seguridad
	            SecurityContextHolder.getContext().setAuthentication(authentication);
	            System.out.println("Autenticado");
	            
	         // Guardar el contexto de seguridad en la sesión
	            HttpSession session = request.getSession(true);
	            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

	            
	            retorno="redirect:/";
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("Hubo error al autenticar");
	            redirectAttributes.addFlashAttribute("message", "Registration successful, but login failed.");
	            retorno = "redirect:/login";
	        }

		}
		return retorno;
	}
	
	@GetMapping("/edit{id}")
	public String editarUsuarioForm(@PathVariable("id") String id, Model model) {
		
		String retorno="";
		Optional<Usuario> user = userRepo.findById(id);
		System.out.println("recogiendo el usuario"+ user.get().toString());
		if (user.isPresent()) {
			model.addAttribute("usuarioEdit", user.get());
			retorno = "form";
		} else {
			retorno = "redirect:/form";
		}
		return retorno;
	}
	
	@PostMapping("/edit/submit")
	public String editarUsuarioSubmit(@Valid @ModelAttribute("userForm") Usuario editarUsuario, BindingResult br) {
		String retorno = "";
		System.out.println("Modificando: "+editarUsuario.toString());
		if (br.hasErrors()){
			retorno = "form";
		} else {
			userService.edit(editarUsuario);
			retorno="redirect:/usersList";
		}
		
		return retorno;
	}
	
	@GetMapping("/userlist/delete")
	public String deleteUser(@RequestParam String id, Model model) {
		Optional<Usuario> user = userRepo.findById(id);
		if (user.isPresent()) {
			System.out.println("Se ha borrado al usuario: ");
			System.out.println(user.get().getId().toString()+" y los comentarios y asociados");
			List<Comentario> commentsDelete = commentRepo.findAllByUsuario(id);
			for (Comentario comment : commentsDelete) {
				commentRepo.deleteById(comment.getId());
			}
			userRepo.deleteById(user.get().getId());
		} 
		return "redirect:/usersList";
		
	}
	
	public Usuario getCurrentUsuario() {
        Usuario retorno=null;
        
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            retorno = (Usuario) authentication.getPrincipal();
        }
        return retorno;
    }
	
	@GetMapping("/workList")
	public String listarObras(Model model) {
		String retorno = "";
		Usuario usuario = getCurrentUsuario();
		if (usuario != null && "ADMIN".equals(usuario.getRole())) {
			retorno = "/workList";
			model.addAttribute("obraList", obraRepo.findAll());
		} else {
			System.out.println("El usuario no es administrador");
			retorno = "/";
		}
		return retorno;
	}
	
	@GetMapping("/workForm/book")
	public String insertarLibro(@RequestParam(required=false) String failure, Model model) {
		if (failure != null) {
			model.addAttribute("failure",failure);
		}
		
		List<String> tags = obraService.findAllDifferentTemas();
		System.out.println("Tags recogidos: "+tags.toString());

		model.addAttribute("tagsJson", tags);		
		model.addAttribute("type","BOOK");
		return "workForm";
	}
	
	@PostMapping("/workForm/bookSubmit")
	public String insertarLibroSubmit(@Valid @ModelAttribute("WorkForm") Obra insertarObra,@RequestParam("tags") String tags, BindingResult br) {
		String retorno = "";
		
		Optional<Obra> test = obraRepo.findByIsbn(insertarObra.getIsbn());
		if (br.hasErrors() || test.isPresent()){
			// This could use to give mor information to the user
			retorno = "redirect:/workForm/book?failure=Problems to insert the new book or it exist";
		} else {
			// converts the string in a List of strings separated by comas and insert in the object
			List<String> tagsList = Arrays.asList(tags.split(","));
			insertarObra.setTemas(tagsList);
			
			System.out.println("Insertando "+insertarObra.toString());
			insertarObra.setTipo("BOOK");
			obraRepo.save(insertarObra);
			retorno="redirect:/workList";
		}
		
		return retorno;
	}
	
	@GetMapping("/workForm/article")
	public String insertarArticulo(@RequestParam(required=false) String failure, Model model) {
		if (failure != null) {
			model.addAttribute("failure",failure);
		}
		
		List<String> tags = obraService.findAllDifferentTemas();
		System.out.println("Tags recogidos: "+tags.toString());

		model.addAttribute("tagsJson", tags);
		model.addAttribute("type","ARTICLE");
		return "workForm";
	}
	
	@PostMapping("/workForm/articleSubmit")
	public String insertarArticuloSubmit(@Valid @ModelAttribute("WorkForm") Obra insertarObra,@RequestParam("tags") String tags, BindingResult br) {
		String retorno = "";
		
		Optional<Obra> test = obraRepo.findByIsbn(insertarObra.getIsbn());
		if (br.hasErrors() || test.isPresent()){
			// This could use to bring mor information to the user
			retorno = "redirect:/workForm/book?failure=Problems to insert the new book or it exist";
		} else {
			
			// converts the string in a List of strings separated by comas and insert in the object
			List<String> tagsList = Arrays.asList(tags.split(","));
			insertarObra.setTemas(tagsList);
			
			System.out.println("Insertando "+insertarObra.toString());
			insertarObra.setTipo("ARTICLE");
			obraRepo.save(insertarObra);
			retorno="redirect:/workList";
		}
		
		return retorno;
	}
		
	@GetMapping("/workList/delete{id}")
	public String borrarObra(Long id) {
		Optional<Obra> obra = obraRepo.findById(id);
		if (obra.isPresent()) {
			System.out.println("Se ha borrado la obra: ");
			System.out.println(obra.get().getIsbn()+obra.get().getTitulo()+"\n y los comentarios y respuestas asociados a ella");
			
			List<Comentario> commentsDelete = commentRepo.findAllByObra(id); // recogemos todos los comentarios y respuestas de esa obra para borrarlos y evitar entradas fantasma
			for (Comentario comment : commentsDelete) {
				commentRepo.deleteById(comment.getId());
			}
			obraRepo.deleteByIsbn(obra.get().getIsbn());
		} 
		return "redirect:/workList";
	}
	
	@GetMapping("/workList/edit{id}")
	public String editarObra(@PathVariable("id") Long id, Model model) {
		String retorno="";
		if (id!=null) {
			
			Optional<Obra> obra = obraRepo.findById(id);	
		
			if (obra!=null && obra.isPresent()) {
				if (obra.get().getTipo().equalsIgnoreCase("BOOK")) {
					retorno="redirect:/workEdit/book?id="+id;
				} else {
					retorno="redirect:/workEdit/article?id="+id;
				}
			} else {
				System.out.println("No se encontró la obra con el isbn: "+id);
				retorno="redirect:/workList";
			}
		} else {
			System.out.println("No se ha recibido id");
			retorno="redirect:/workList";
		}
		return retorno;
	}


	
	@GetMapping("/workEdit/book")
	public String editarLibro(@RequestParam Long id, Model model) {
		String retorno="";
		if (id != null) {
			Optional<Obra> obra = obraRepo.findById(id);
			if (obra != null && obra.isPresent()) {
					
				List<String> tags = obraService.findAllDifferentTemas();
				System.out.println("Tags recogidos: "+tags.toString());

				model.addAttribute("tagsJson", tags);
				model.addAttribute("obraEdit", obra.get());
				model.addAttribute("type", obra.get().getTipo().toUpperCase());
				retorno = "workEdit";
			} else {
				System.out.println("No se ha encontrado la obra para editar");
				retorno = "redirect:/workList";
			}
		} else {
			System.out.println("No se ha recibido id para editar");
			retorno = "redirect:/workList";
		}
		
		return retorno;
	}
	
	@PostMapping("/workEdit/bookSubmit")
	public String editarLibroSubmit(@Valid @ModelAttribute("workEdit") Obra editedWork,@RequestParam("tags") String tags, BindingResult br) {
		String retorno="";
		
		System.out.println("Insertando "+editedWork.toString());
		Optional<Obra> test = obraRepo.findByIsbn(editedWork.getIsbn());
		if (br.hasErrors()){
			// This could use to give more information to the user
			retorno = "redirect:/workEdit/book?failure=Problems to update the book or it exist";
		} else  if (test.isPresent()){
			
			// converts the string in a List of strings separated by comas and insert in the object
			List<String> tagsList = Arrays.asList(tags.split(","));
			editedWork.setTemas(tagsList);
			
			obraRepo.deleteByIsbn(editedWork.getIsbn());
			editedWork.setTipo("BOOK");
			obraRepo.save(editedWork);
			retorno="redirect:/workList";
			
		} else {
			
			editedWork.setTipo("BOOK");
			obraRepo.save(editedWork);
			retorno="redirect:/workList";
		}
		
		return retorno;
	}
	
	@GetMapping("/workEdit/article{id}")
	public String editarArticulo(@RequestParam Long id, Model model) {
		String retorno="";
		if (id != null) {
			Optional<Obra> obra = obraRepo.findById(id);
			if (obra != null && obra.isPresent()) {
				
				List<String> tags = obraService.findAllDifferentTemas();
				System.out.println("Tags recogidos: "+tags.toString());

				model.addAttribute("tagsJson", tags);
				model.addAttribute("obraEdit", obra.get());
				model.addAttribute("type", obra.get().getTipo().toUpperCase());
				retorno = "workEdit";
			}
		} else {
			System.out.println("no se ha recibido id para editar");
			retorno = "redirect:/workList";
		}
		
		return retorno;
	}
	
	@PostMapping("/workEdit/articleSubmit")
	public String editarArticuloSubmit(@Valid @ModelAttribute("workEdit") Obra editedWork,@RequestParam("tags") String tags, BindingResult br) {
		String retorno="";
		
		System.out.println("Insertando "+editedWork.toString());
		Optional<Obra> test = obraRepo.findByIsbn(editedWork.getIsbn());
		if (br.hasErrors()){
			// This could use to give more information to the user
			retorno = "redirect:/workEdit/article?failure=Problems to update the book or it exist";
		} else  if (test.isPresent()){
			
			// converts the string in a List of strings separated by comas and insert in the object
			List<String> tagsList = Arrays.asList(tags.split(","));
			editedWork.setTemas(tagsList);
			
			obraRepo.deleteByIsbn(editedWork.getIsbn());
			editedWork.setTipo("ARTICLE");
			obraRepo.save(editedWork);
			retorno="redirect:/workList";
			
		} else {
			
			editedWork.setTipo("ARTICLE");
			obraRepo.save(editedWork);
			retorno="redirect:/workList";
		}
		
		return retorno;
	}
	
	@GetMapping("/commentList")
	public String listarComentarios(Model model) {
		String retorno="";
		List<Comentario> comments = commentRepo.findAll();
		if (comments != null && !comments.isEmpty()) {
			model.addAttribute("comments", comments);
			retorno="commentList";
		} else {
			System.out.println("No se encontraron comentarios volviendo a index");
			retorno="/";
		}
		return retorno;
	}
	
	@GetMapping("/banComment")
	public String bannearComentario(@RequestParam String commentId, @RequestParam Long obraId, @RequestParam String origin) {
		String retorno="";
		commentService.banComment(commentId);
		if (origin.equals("workShow")) {
			retorno = "redirect:/workShow?id="+obraId; 
		} else {
			retorno = "redirect:/commentList";
		}
		return retorno;
	}
	
	@GetMapping("/unbanComment")
	public String desbannearComentario(@RequestParam String id) {
		commentService.unbanComment(id);
		return "redirect:/commentList";
	}
	
	
	
}
