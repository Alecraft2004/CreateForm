package com.encuesta.EncuestaYVotacion.web;

import com.encuesta.EncuestaYVotacion.service.AuthService;
import com.encuesta.EncuestaYVotacion.web.dto.RegistroDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/login")
  public String login() { return "login"; }

  @GetMapping("/register")
  public String registerForm(Model model) {
    model.addAttribute("form", new RegistroDTO());
    return "register";
  }

  @PostMapping("/register")
  public String register(@Valid @ModelAttribute("form") RegistroDTO form,
                         BindingResult br,
                         Model model) {
    if (br.hasErrors()) return "register";
    try {
      authService.register(form);
      return "redirect:/login?registered";
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "register";
    }
  }
}
