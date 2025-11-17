package com.encuesta.EncuestaYVotacion.model;

import jakarta.persistence.*;

@Entity
@Table(name = "opcion_encuesta", schema = "public")
public class OpcionEncuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opcion_encuesta_seq")
    @SequenceGenerator(name = "opcion_encuesta_seq", sequenceName = "opcion_encuesta_id_opcion_seq", allocationSize = 1)
    @Column(name = "id_opcion")
    private Integer id;

    @Column(name = "texto_opcion", nullable = false, length = 200)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public Pregunta getPregunta() { return pregunta; }
    public void setPregunta(Pregunta pregunta) { this.pregunta = pregunta; }
}

