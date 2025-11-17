package com.encuesta.EncuestaYVotacion.service;

import com.encuesta.EncuestaYVotacion.model.Usuario;
import com.encuesta.EncuestaYVotacion.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {

  private final UsuarioRepository repo;

  public UsuarioDetailsService(UsuarioRepository repo) {
    this.repo = repo;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Usuario u = repo.findByCorreo(username)
        .orElseThrow(() -> new UsernameNotFoundException("No existe correo: " + username));

    String role = "ROLE_USER";
    if ("administrador".equalsIgnoreCase(u.getTipoUsuario())) role = "ROLE_ADMIN";

    return User.withUsername(u.getCorreo())
        .password(u.getContrasena())
        .authorities(List.of(new SimpleGrantedAuthority(role)))
        .build();
  }
}

