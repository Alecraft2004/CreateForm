package com.encuesta.EncuestaYVotacion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "votacion", schema = "public")
public class Votacion {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "votacion_seq")
    @SequenceGenerator(name = "votacion_seq", sequenceName = "votacion_id_votacion_seq", allocationSize = 1)
    @Column(name = "id_votacion")
    private Integer id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @Column(name = "estado", length = 20)
    private String estado; // 'activa'|'inactiva'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "votacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpcionVotacion> opciones = new ArrayList<>();

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public List<OpcionVotacion> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionVotacion> opciones) { this.opciones = opciones; }
}
