package com.encuesta.EncuestaYVotacion.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "pregunta", schema = "public")
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pregunta_seq")
    @SequenceGenerator(name = "pregunta_seq", sequenceName = "pregunta_id_pregunta_seq", allocationSize = 1)
    @Column(name = "id_pregunta")
    private Integer id;

    @Column(name = "texto_pregunta", nullable = false)
    private String texto;

    @Column(name = "tipo_pregunta", length = 50)
    private String tipo; // guardado como texto

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_encuesta", nullable = false)
    private Encuesta encuesta;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpcionEncuesta> opciones = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Encuesta getEncuesta() { return encuesta; }
    public void setEncuesta(Encuesta encuesta) { this.encuesta = encuesta; }
    public List<OpcionEncuesta> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionEncuesta> opciones) { this.opciones = opciones; }
}
