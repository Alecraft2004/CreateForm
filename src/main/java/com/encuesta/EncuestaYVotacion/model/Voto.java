package com.encuesta.EncuestaYVotacion.model;

import jakarta.persistence.*;

@Entity
@Table(name = "voto", schema = "public")
public class Voto {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "voto_seq")
    @SequenceGenerator(name = "voto_seq", sequenceName = "voto_id_voto_seq", allocationSize = 1)
    @Column(name = "id_voto")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_votacion", nullable = false)
    private Votacion votacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion", nullable = false)
    private OpcionVotacion opcion;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Votacion getVotacion() { return votacion; }
    public void setVotacion(Votacion votacion) { this.votacion = votacion; }
    public OpcionVotacion getOpcion() { return opcion; }
    public void setOpcion(OpcionVotacion opcion) { this.opcion = opcion; }
}
