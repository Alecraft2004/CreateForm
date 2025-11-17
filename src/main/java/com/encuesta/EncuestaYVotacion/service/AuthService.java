package com.encuesta.EncuestaYVotacion.service;

import com.encuesta.EncuestaYVotacion.model.Usuario;
import com.encuesta.EncuestaYVotacion.repository.UsuarioRepository;
import com.encuesta.EncuestaYVotacion.web.dto.RegistroDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UsuarioRepository repo;
  private final PasswordEncoder encoder;

  public AuthService(UsuarioRepository repo, PasswordEncoder encoder) {
    this.repo = repo;
    this.encoder = encoder;
  }

  @Transactional
  public void register(RegistroDTO dto) {
    if (repo.existsByCorreo(dto.getCorreo())) {
      throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
    }
    if (!dto.getPassword().equals(dto.getConfirmPassword())) {
      throw new IllegalArgumentException("Las contraseñas no coinciden.");
    }
    if (!"administrador".equalsIgnoreCase(dto.getTipoUsuario())
        && !"participante".equalsIgnoreCase(dto.getTipoUsuario())) {
      throw new IllegalArgumentException("Tipo de usuario inválido.");
    }

    Usuario u = new Usuario();
    u.setNombre(dto.getNombre());
    u.setCorreo(dto.getCorreo());
    u.setContrasena(encoder.encode(dto.getPassword())); // HASH
    u.setTipoUsuario(dto.getTipoUsuario().toLowerCase());

    repo.save(u); // INSERT en public.usuario
  }
}
