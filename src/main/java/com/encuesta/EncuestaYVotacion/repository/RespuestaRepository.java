package com.encuesta.EncuestaYVotacion.repository;

import com.encuesta.EncuestaYVotacion.model.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {
    @Query("SELECT r FROM Respuesta r WHERE r.pregunta.encuesta.id = :encuestaId")
    List<Respuesta> findByEncuestaId(@Param("encuestaId") Integer encuestaId);
    
    List<Respuesta> findByUsuarioId(Integer usuarioId);
}
