package com.encuesta.EncuestaYVotacion.web.dto;

import java.util.List;

public class EncuestaDetalleDTO {
    public Integer id;
    public String titulo;
    public String descripcion;
    public List<PreguntaDetalleDTO> preguntas;

    public static class PreguntaDetalleDTO {
        public Integer id;
        public String texto;
        public String tipo;
        public List<OpcionDetalleDTO> opciones;
    }

    public static class OpcionDetalleDTO {
        public Integer id;
        public String texto;
    }
}
