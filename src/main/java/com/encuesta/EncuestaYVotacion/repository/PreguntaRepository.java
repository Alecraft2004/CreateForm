package com.encuesta.EncuestaYVotacion.repository;

import com.encuesta.EncuestaYVotacion.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {
}
