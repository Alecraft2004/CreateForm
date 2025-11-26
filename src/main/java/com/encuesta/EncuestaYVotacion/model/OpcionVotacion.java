package com.encuesta.EncuestaYVotacion.model;

import jakarta.persistence.*;

@Entity
@Table(name = "opcion_votacion", schema = "public")
public class OpcionVotacion {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opcion_votacion_seq")
    @SequenceGenerator(name = "opcion_votacion_seq", sequenceName = "opcion_votacion_id_opcion_seq", allocationSize = 1)
    @Column(name = "id_opcion")
    private Integer id;

    @Column(name = "texto_opcion", nullable = false, length = 200)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_votacion", nullable = false)
    private Votacion votacion;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public Votacion getVotacion() { return votacion; }
    public void setVotacion(Votacion votacion) { this.votacion = votacion; }
}
