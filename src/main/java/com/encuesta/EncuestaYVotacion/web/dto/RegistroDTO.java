package com.encuesta.EncuestaYVotacion.web.dto;

import jakarta.validation.constraints.*;

public class RegistroDTO {
  @NotBlank private String nombre;
  @Email @NotBlank private String correo;
  @Size(min = 6) private String password;
  @Size(min = 6) private String confirmPassword;
  @NotBlank private String tipoUsuario; // 'administrador' | 'participante'

  // getters y setters
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getCorreo() { return correo; }
  public void setCorreo(String correo) { this.correo = correo; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public String getConfirmPassword() { return confirmPassword; }
  public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
  public String getTipoUsuario() { return tipoUsuario; }
  public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
}


