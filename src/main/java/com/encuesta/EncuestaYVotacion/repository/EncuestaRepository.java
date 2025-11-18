package com.encuesta.EncuestaYVotacion.repository;

import com.encuesta.EncuestaYVotacion.model.Encuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface EncuestaRepository extends JpaRepository<Encuesta, Integer> {
    List<Encuesta> findByEstado(String estado);
    List<Encuesta> findByEsVotacion(boolean esVotacion);
    List<Encuesta> findByEstadoAndEsVotacion(String estado, boolean esVotacion);
}
