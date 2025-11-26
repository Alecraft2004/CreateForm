package com.encuesta.EncuestaYVotacion.repository;

import com.encuesta.EncuestaYVotacion.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VotoRepository extends JpaRepository<Voto, Integer> {
    List<Voto> findByVotacionId(Integer votacionId);
    long countByVotacionIdAndUsuarioId(Integer votacionId, Integer usuarioId);
}
