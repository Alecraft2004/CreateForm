package com.encuesta.EncuestaYVotacion.service;

import com.encuesta.EncuestaYVotacion.model.*;
import com.encuesta.EncuestaYVotacion.repository.*;
import com.encuesta.EncuestaYVotacion.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EncuestaService {

    private final EncuestaRepository encuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RespuestaRepository respuestaRepository;
    private final PreguntaRepository preguntaRepository;
    private final OpcionEncuestaRepository opcionEncuestaRepository;

    public EncuestaService(EncuestaRepository encuestaRepository, UsuarioRepository usuarioRepository,
                           RespuestaRepository respuestaRepository, PreguntaRepository preguntaRepository,
                           OpcionEncuestaRepository opcionEncuestaRepository) {
        this.encuestaRepository = encuestaRepository;
        this.usuarioRepository = usuarioRepository;
        this.respuestaRepository = respuestaRepository;
        this.preguntaRepository = preguntaRepository;
        this.opcionEncuestaRepository = opcionEncuestaRepository;
    }

    @Transactional
    public Integer crear(EncuestaDTO dto, String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Encuesta enc = new Encuesta();
        enc.setTitulo(dto.titulo);
        enc.setDescripcion(dto.descripcion);
        enc.setTipoEncuesta(dto.tipoEncuesta);
        enc.setEstado("activa");
        enc.setEsVotacion(Boolean.TRUE.equals(dto.esVotacion));
        enc.setUsuario(usuario);

        if (dto.preguntas != null) {
            for (EncuestaDTO.PreguntaDTO p : dto.preguntas) {
                Pregunta pr = new Pregunta();
                pr.setEncuesta(enc);
                pr.setTexto(p.texto);
                pr.setTipo(normalizarTipo(p.tipo));

                if (p.opciones != null) {
                    for (EncuestaDTO.OpcionDTO o : p.opciones) {
                        OpcionEncuesta op = new OpcionEncuesta();
                        op.setPregunta(pr);
                        op.setTexto(o.texto);
                        pr.getOpciones().add(op);
                    }
                }
                enc.getPreguntas().add(pr);
            }
        }
        return encuestaRepository.save(enc).getId();
    }

    public List<EncuestaResumenDTO> listar(String estado, Boolean esVotacion, String correoUsuario) {
        List<Encuesta> list;
        if (esVotacion != null && estado != null && !estado.isBlank()) {
            list = encuestaRepository.findByEstadoAndEsVotacion(estado, esVotacion);
        } else if (esVotacion != null) {
            list = encuestaRepository.findByEsVotacion(esVotacion);
        } else if (estado != null && !estado.isBlank()) {
            list = encuestaRepository.findByEstado(estado);
        } else {
            list = encuestaRepository.findAll();
        }

        Usuario usuario = correoUsuario != null ? usuarioRepository.findByCorreo(correoUsuario).orElse(null) : null;
        List<EncuestaResumenDTO> out = new ArrayList<>();
        
        for (Encuesta e : list) {
            EncuestaResumenDTO r = new EncuestaResumenDTO();
            r.id = e.getId();
            r.titulo = e.getTitulo();
            r.descripcion = e.getDescripcion();
            r.tipoEncuesta = e.getTipoEncuesta();
            r.estado = e.getEstado();
            r.preguntas = e.getPreguntas() != null ? e.getPreguntas().size() : 0;

            if (usuario != null && ("administrador".equals(usuario.getTipoUsuario()) || e.getUsuario().getId().equals(usuario.getId()))) {
                r.esPropietario = true;
            }
            if (usuario != null && "administrador".equals(usuario.getTipoUsuario())) {
                r.puedeBorrar = true;
            }
            out.add(r);
        }
        return out;
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!encuestaRepository.existsById(id)) {
            throw new RuntimeException("Encuesta no encontrada");
        }
        List<Respuesta> respuestas = respuestaRepository.findByEncuestaId(id);
        if (!respuestas.isEmpty()) {
            respuestaRepository.deleteAll(respuestas);
        }
        encuestaRepository.deleteById(id);
    }

    @Transactional
    public void actualizar(Integer id, EncuestaDTO dto) {
        Encuesta enc = encuestaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encuesta no encontrada"));

        enc.setTitulo(dto.titulo);
        enc.setDescripcion(dto.descripcion);
        enc.setTipoEncuesta(dto.tipoEncuesta);

        List<Respuesta> respuestas = respuestaRepository.findByEncuestaId(id);
        if (!respuestas.isEmpty()) {
            respuestaRepository.deleteAll(respuestas);
        }

        enc.getPreguntas().clear();

        if (dto.preguntas != null) {
            for (EncuestaDTO.PreguntaDTO p : dto.preguntas) {
                Pregunta pr = new Pregunta();
                pr.setEncuesta(enc);
                pr.setTexto(p.texto);
                pr.setTipo(normalizarTipo(p.tipo));

                if (p.opciones != null) {
                    for (EncuestaDTO.OpcionDTO o : p.opciones) {
                        OpcionEncuesta op = new OpcionEncuesta();
                        op.setPregunta(pr);
                        op.setTexto(o.texto);
                        pr.getOpciones().add(op);
                    }
                }
                enc.getPreguntas().add(pr);
            }
        }
        encuestaRepository.save(enc);
    }

    public EncuestaDetalleDTO obtenerDetalle(Integer id) {
        return encuestaRepository.findById(id)
                .map(this::mapToDetalleDTO)
                .orElseThrow(() -> new RuntimeException("Encuesta no encontrada"));
    }

    @Transactional
    public void participar(Integer id, RespuestaSubmissionDTO dto, String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Encuesta encuesta = encuestaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encuesta no encontrada"));

        if (dto.respuestas == null || dto.respuestas.isEmpty()) {
            throw new IllegalArgumentException("No se enviaron respuestas");
        }

        for (RespuestaSubmissionDTO.RespuestaItemDTO item : dto.respuestas) {
            Respuesta r = new Respuesta();
            r.setUsuario(usuario);
            Pregunta p = preguntaRepository.findById(item.preguntaId).orElse(null);
            if (p == null || !p.getEncuesta().getId().equals(id)) continue;
            r.setPregunta(p);

            if (item.opcionId != null) {
                OpcionEncuesta op = opcionEncuestaRepository.findById(item.opcionId).orElse(null);
                if (op != null && op.getPregunta().getId().equals(p.getId())) {
                    r.setOpcion(op);
                }
            }
            r.setRespuestaTexto(item.texto);
            respuestaRepository.save(r);
        }
    }

    public List<RespuestaAdminDTO> verRespuestas(Integer id, String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Encuesta encuesta = encuestaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encuesta no encontrada"));

        if (!"administrador".equals(usuario.getTipoUsuario()) && !encuesta.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No autorizado");
        }

        List<Respuesta> respuestas = respuestaRepository.findByEncuestaId(id);
        List<RespuestaAdminDTO> out = new ArrayList<>();
        for (Respuesta r : respuestas) {
            RespuestaAdminDTO d = new RespuestaAdminDTO();
            d.idRespuesta = r.getId();
            d.usuario = r.getUsuario().getNombre();
            d.pregunta = r.getPregunta().getTexto();
            d.opcion = r.getOpcion() != null ? r.getOpcion().getTexto() : null;
            d.texto = r.getRespuestaTexto();
            out.add(d);
        }
        return out;
    }

    private EncuestaDetalleDTO mapToDetalleDTO(Encuesta e) {
        EncuestaDetalleDTO d = new EncuestaDetalleDTO();
        d.id = e.getId();
        d.titulo = e.getTitulo();
        d.descripcion = e.getDescripcion();
        d.tipoEncuesta = e.getTipoEncuesta();
        d.preguntas = new ArrayList<>();
        if (e.getPreguntas() != null) {
            for (Pregunta p : e.getPreguntas()) {
                EncuestaDetalleDTO.PreguntaDetalleDTO pd = new EncuestaDetalleDTO.PreguntaDetalleDTO();
                pd.id = p.getId();
                pd.texto = p.getTexto();
                pd.tipo = p.getTipo();
                pd.opciones = new ArrayList<>();
                if (p.getOpciones() != null) {
                    for (OpcionEncuesta op : p.getOpciones()) {
                        EncuestaDetalleDTO.OpcionDetalleDTO od = new EncuestaDetalleDTO.OpcionDetalleDTO();
                        od.id = op.getId();
                        od.texto = op.getTexto();
                        pd.opciones.add(od);
                    }
                }
                d.preguntas.add(pd);
            }
        }
        return d;
    }

    private String normalizarTipo(String t) {
        if (t == null) return "corta";
        return t;
    }
}
