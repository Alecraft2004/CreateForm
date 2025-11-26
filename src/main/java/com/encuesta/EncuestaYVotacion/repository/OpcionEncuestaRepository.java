package com.encuesta.EncuestaYVotacion.repository;

import com.encuesta.EncuestaYVotacion.model.OpcionEncuesta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcionEncuestaRepository extends JpaRepository<OpcionEncuesta, Integer> {
}
