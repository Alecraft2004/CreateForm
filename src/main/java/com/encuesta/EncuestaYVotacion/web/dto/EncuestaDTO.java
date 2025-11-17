package com.encuesta.EncuestaYVotacion.web.dto;

import java.util.*;

public class EncuestaDTO {
    public String titulo;
    public String descripcion;
    public Boolean esVotacion; // null = default false
    public List<PreguntaDTO> preguntas;

    public static class PreguntaDTO {
        public String texto;
        public String tipo; // 'corta','parrafo','opcion_multiple','casillas','desplegable'
        public boolean requerida;
        public List<OpcionDTO> opciones;
    }

    public static class OpcionDTO {
        public String texto;
        public boolean correcta;
    }
}
