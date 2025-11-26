package com.encuesta.EncuestaYVotacion.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "encuesta", schema = "public")
public class Encuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "encuesta_seq")
    @SequenceGenerator(name = "encuesta_seq", sequenceName = "encuesta_id_encuesta_seq", allocationSize = 1)
    @Column(name = "id_encuesta")
    private Integer id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "estado", length = 20)
    private String estado; // 'activa'|'inactiva'

    @Column(name = "es_votacion", nullable = false)
    private boolean esVotacion = false;

    @Column(name = "tipo_encuesta", length = 50)
    private String tipoEncuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "encuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pregunta> preguntas = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isEsVotacion() { return esVotacion; }
    public void setEsVotacion(boolean esVotacion) { this.esVotacion = esVotacion; }
    public String getTipoEncuesta() { return tipoEncuesta; }
    public void setTipoEncuesta(String tipoEncuesta) { this.tipoEncuesta = tipoEncuesta; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public List<Pregunta> getPreguntas() { return preguntas; }
    public void setPreguntas(List<Pregunta> preguntas) { this.preguntas = preguntas; }
}
