package com.sympos2.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sympos2.models.Usuario;
import com.sympos2.repositories.UserRepository;

import jakarta.validation.Valid;

@Controller
public class MainController {
	
	@Autowired
	private UserRepository UserRepo;
	
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
	
	@GetMapping("register")
	public String registro(Model model) {
		// Here goes the things before the page charges
		return "/register";
	}
	
	@PostMapping("/submit")
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
			return "redirect:/register";  // Redirigir a la página de registro
		} else {
			nuevoUsuario.setRole("STUDENT");
			UserRepo.save(nuevoUsuario);
			retorno="index";
		}
		return retorno;
	}
	
}
