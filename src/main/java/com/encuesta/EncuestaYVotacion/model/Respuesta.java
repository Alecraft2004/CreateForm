package com.encuesta.EncuestaYVotacion.model;

import jakarta.persistence.*;

@Entity
@Table(name = "respuesta", schema = "public")
public class Respuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "respuesta_seq")
    @SequenceGenerator(name = "respuesta_seq", sequenceName = "respuesta_id_respuesta_seq", allocationSize = 1)
    @Column(name = "id_respuesta")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion")
    private OpcionEncuesta opcion;

    @Column(name = "respuesta_texto")
    private String respuestaTexto;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Pregunta getPregunta() { return pregunta; }
    public void setPregunta(Pregunta pregunta) { this.pregunta = pregunta; }
    public OpcionEncuesta getOpcion() { return opcion; }
    public void setOpcion(OpcionEncuesta opcion) { this.opcion = opcion; }
    public String getRespuestaTexto() { return respuestaTexto; }
    public void setRespuestaTexto(String respuestaTexto) { this.respuestaTexto = respuestaTexto; }
}
