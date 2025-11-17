package com.encuesta.EncuestaYVotacion.web;

import com.encuesta.EncuestaYVotacion.model.Usuario;
import com.encuesta.EncuestaYVotacion.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {

  private final UsuarioRepository usuarioRepository;

  public GlobalModelAttributes(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @ModelAttribute("usuarioActual")
  public Usuario usuarioActual(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) return null;
    String correo = authentication.getName();
    return usuarioRepository.findByCorreo(correo).orElse(null);
  }
}

