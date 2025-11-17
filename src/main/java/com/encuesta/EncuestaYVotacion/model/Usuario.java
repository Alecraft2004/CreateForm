package com.encuesta.EncuestaYVotacion.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario", schema = "public")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
  @SequenceGenerator(name = "usuario_seq", sequenceName = "usuario_id_usuario_seq", allocationSize = 1)
  @Column(name = "id_usuario")
  private Integer id;

  @Column(name = "nombre", nullable = false, length = 100)
  private String nombre;

  @Column(name = "correo_electronico", nullable = false, unique = true, length = 150)
  private String correo;

  @Column(name = "contrasena", nullable = false, length = 200)
  private String contrasena; // almacenar HASH BCrypt

  @Column(name = "tipo_usuario", nullable = false, length = 20)
  private String tipoUsuario; // 'administrador' | 'participante'

  // getters y setters
  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getCorreo() { return correo; }
  public void setCorreo(String correo) { this.correo = correo; }
  public String getContrasena() { return contrasena; }
  public void setContrasena(String contrasena) { this.contrasena = contrasena; }
  public String getTipoUsuario() { return tipoUsuario; }
  public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
}
