package com.encuesta.EncuestaYVotacion.repository;

import com.encuesta.EncuestaYVotacion.model.Votacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VotacionRepository extends JpaRepository<Votacion, Integer> {
    List<Votacion> findByEstado(String estado);
}
