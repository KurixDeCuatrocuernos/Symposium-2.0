package com.sympos2.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sympos2.models.Usuario;
import com.sympos2.repositories.UserRepository;
import com.sympos2.services.UserService;

import jakarta.validation.Valid;

@Controller
public class MainController {
	
	@Autowired
	private UserRepository UserRepo;
	
	@Autowired
	private UserService service;
	
//	@Autowired
//	private StorageService storage;
	
	@GetMapping("/")
	public String index(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated()) {
			model.addAttribute("username", auth.getName());
		} else {
			model.addAttribute("username", "invitado");
		}
		return "index";
	}
	// mappings for user
	@GetMapping("admin-zone-users-list")
	public String listado(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated()) {
			model.addAttribute("username", auth.getName());
		} else {
			model.addAttribute("username", "invitado");
		}
		model.addAttribute("listaUsuarios", UserRepo.findAll());
		return "admin-zone-users-list";
	}
	
	@GetMapping("form")
	public String registro(Model model) {
		
		return "/form";
	}
	
	@PostMapping("/form/submit")
	public String handleFormSubmit(@Valid @ModelAttribute("userForm") Usuario nuevoUsuario, BindingResult br, RedirectAttributes redirectAttributes) {
		String retorno="";
		String mensaje="";
		boolean check=true;
		
		if (br.hasErrors()) {
			check=false;
			mensaje+="Errors in register\n";
		} 
		if (nuevoUsuario.getName()==null) {
			check=false;
			mensaje+="You needs an username";
		}
		if (nuevoUsuario.getFechaNac()==null) {
			check=false;
			mensaje+="You needs a birthday date\n";
		}
		if (nuevoUsuario.getEmail()==null || !nuevoUsuario.getEmail().contains("@")) {
			check=false;
			mensaje+="You needs a valid email\n";
		}
		
		if (check==false) {
			redirectAttributes.addFlashAttribute("message", mensaje);  // Usar addFlashAttribute
			return "redirect:/form";  // Redirigir a la página de registro
		} else {
			nuevoUsuario.setRole("ROLE_STUDENT");
			UserRepo.save(nuevoUsuario);
			retorno="index";
		}
		return retorno;
	}
	
	@GetMapping("/edit{id}")
	public String editarUsuarioForm(@PathVariable("id") String id, Model model) {
		String retorno="";
		Optional<Usuario> user = UserRepo.findById(id);
		System.out.println("recogiendo el usuario"+ user.get().toString());
		if (user.isPresent()) {
			model.addAttribute("usuarioEdit", user.get());
			retorno = "form";
		} else {
			retorno = "redirect:/form";
		}
		return retorno;
	}
	
	@PostMapping("edit/submit")
	public String editarUsuarioSubmit(@Valid @ModelAttribute("userForm") Usuario editarUsuario, BindingResult br) {
		String retorno = "";
		System.out.println("Modificando: "+editarUsuario.toString());
		if (br.hasErrors()){
			retorno = "form";
		} else {
			service.edit(editarUsuario);
			retorno="redirect:/admin-zone-users-list";
		}
		
		return retorno;
	}
	
	@GetMapping("/userlist/delete")
	public String deleteUser(@RequestParam String id, Model model) {
		Optional<Usuario> user = UserRepo.findById(id);
		if (user.isPresent()) {
			System.out.println("Se ha borrado al usuario: ");
			System.out.println(user.get().getId().toString());
			UserRepo.deleteById(user.get().getId());
		} 
		return "redirect:/admin-zone-users-list";
		
	}
}
