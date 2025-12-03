package com.encuesta.EncuestaYVotacion.service;

import com.encuesta.EncuestaYVotacion.model.*;
import com.encuesta.EncuestaYVotacion.repository.*;
import com.encuesta.EncuestaYVotacion.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class VotacionService {

    private final VotacionRepository votacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final VotoRepository votoRepository;
    private final OpcionVotacionRepository opcionVotacionRepository;

    public VotacionService(VotacionRepository votacionRepository, UsuarioRepository usuarioRepository,
                           VotoRepository votoRepository, OpcionVotacionRepository opcionVotacionRepository) {
        this.votacionRepository = votacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.votoRepository = votoRepository;
        this.opcionVotacionRepository = opcionVotacionRepository;
    }

    @Transactional
    public Integer crear(EncuestaDTO dto, String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Votacion v = new Votacion();
        v.setTitulo(dto.titulo);
        v.setEstado("activa");
        v.setUsuario(usuario);

        if (dto.preguntas != null && !dto.preguntas.isEmpty()) {
            EncuestaDTO.PreguntaDTO p = dto.preguntas.get(0);
            v.setTipo(p.tipo);
            if (p.opciones != null) {
                for (EncuestaDTO.OpcionDTO o : p.opciones) {
                    OpcionVotacion ov = new OpcionVotacion();
                    ov.setTexto(o.texto);
                    ov.setVotacion(v);
                    v.getOpciones().add(ov);
                }
            }
        }
        return votacionRepository.save(v).getId();
    }

    public List<EncuestaResumenDTO> listar(String correoUsuario) {
        List<Votacion> list = votacionRepository.findAll();
        Usuario usuario = correoUsuario != null ? usuarioRepository.findByCorreo(correoUsuario).orElse(null) : null;

        List<EncuestaResumenDTO> out = new ArrayList<>();
        for (Votacion v : list) {
            EncuestaResumenDTO r = new EncuestaResumenDTO();
            r.id = v.getId();
            r.titulo = v.getTitulo();
            r.descripcion = "Votación";
            r.tipoEncuesta = "votacion";
            r.estado = v.getEstado();
            r.preguntas = 1;

            if (usuario != null && ("administrador".equals(usuario.getTipoUsuario()) || v.getUsuario().getId().equals(usuario.getId()))) {
                r.esPropietario = true;
            }
            if (usuario != null && "administrador".equals(usuario.getTipoUsuario())) {
                r.puedeBorrar = true;
            }
            out.add(r);
        }
        return out;
    }

    public EncuestaDetalleDTO obtener(Integer id) {
        return votacionRepository.findById(id).map(v -> {
            EncuestaDetalleDTO d = new EncuestaDetalleDTO();
            d.id = v.getId();
            d.titulo = v.getTitulo();
            d.descripcion = "Votación";
            d.tipoEncuesta = "votacion";

            EncuestaDetalleDTO.PreguntaDetalleDTO p = new EncuestaDetalleDTO.PreguntaDetalleDTO();
            p.id = 1;
            p.texto = v.getTitulo();
            p.tipo = v.getTipo() != null ? v.getTipo() : "casillas";
            p.opciones = new ArrayList<>();

            for (OpcionVotacion ov : v.getOpciones()) {
                EncuestaDetalleDTO.OpcionDetalleDTO od = new EncuestaDetalleDTO.OpcionDetalleDTO();
                od.id = ov.getId();
                od.texto = ov.getTexto();
                p.opciones.add(od);
            }
            d.preguntas = Collections.singletonList(p);
            return d;
        }).orElseThrow(() -> new RuntimeException("Votación no encontrada"));
    }

    @Transactional
    public void votar(Integer id, RespuestaSubmissionDTO dto, String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Votacion v = votacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Votación no encontrada"));

        if (votoRepository.countByVotacionIdAndUsuarioId(id, usuario.getId()) > 0) {
            throw new IllegalArgumentException("Ya has votado en esta votación.");
        }

        if (dto.respuestas != null) {
            for (RespuestaSubmissionDTO.RespuestaItemDTO item : dto.respuestas) {
                if (item.opcionId != null) {
                    OpcionVotacion ov = opcionVotacionRepository.findById(item.opcionId).orElse(null);
                    if (ov != null && ov.getVotacion().getId().equals(id)) {
                        Voto voto = new Voto();
                        voto.setUsuario(usuario);
                        voto.setVotacion(v);
                        voto.setOpcion(ov);
                        votoRepository.save(voto);
                    }
                }
            }
        }
    }

    public List<RespuestaAdminDTO> verResultados(Integer id) {
        List<Voto> votos = votoRepository.findByVotacionId(id);
        List<RespuestaAdminDTO> out = new ArrayList<>();
        for (Voto vt : votos) {
            RespuestaAdminDTO d = new RespuestaAdminDTO();
            d.idRespuesta = vt.getId();
            d.usuario = vt.getUsuario().getNombre();
            d.pregunta = vt.getVotacion().getTitulo();
            d.opcion = vt.getOpcion().getTexto();
            out.add(d);
        }
        return out;
    }

    @Transactional
    public void eliminar(Integer id) {
        if (votacionRepository.existsById(id)) {
            votacionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Votación no encontrada");
        }
    }

    @Transactional
    public void actualizar(Integer id, EncuestaDTO dto) {
        Votacion v = votacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Votación no encontrada"));

        v.setTitulo(dto.titulo);

        List<Voto> votos = votoRepository.findByVotacionId(id);
        if (!votos.isEmpty()) {
            votoRepository.deleteAll(votos);
        }

        v.getOpciones().clear();

        if (dto.preguntas != null && !dto.preguntas.isEmpty()) {
            EncuestaDTO.PreguntaDTO p = dto.preguntas.get(0);
            v.setTipo(p.tipo);
            if (p.opciones != null) {
                for (EncuestaDTO.OpcionDTO o : p.opciones) {
                    OpcionVotacion ov = new OpcionVotacion();
                    ov.setTexto(o.texto);
                    ov.setVotacion(v);
                    v.getOpciones().add(ov);
                }
            }
        }
        votacionRepository.save(v);
    }
}
