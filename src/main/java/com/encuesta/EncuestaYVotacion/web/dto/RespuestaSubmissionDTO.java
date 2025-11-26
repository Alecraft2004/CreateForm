package com.encuesta.EncuestaYVotacion.web.dto;

import java.util.List;

public class RespuestaSubmissionDTO {
    public List<RespuestaItemDTO> respuestas;

    public static class RespuestaItemDTO {
        public Integer preguntaId;
        public Integer opcionId; // Nullable
        public String texto; // Nullable
    }
}
