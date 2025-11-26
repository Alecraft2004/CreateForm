package com.encuesta.EncuestaYVotacion.repository;

import com.encuesta.EncuestaYVotacion.model.OpcionVotacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcionVotacionRepository extends JpaRepository<OpcionVotacion, Integer> {
}
