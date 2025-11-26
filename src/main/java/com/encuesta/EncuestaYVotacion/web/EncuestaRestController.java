package com.encuesta.EncuestaYVotacion.web;

import com.encuesta.EncuestaYVotacion.model.*;
import com.encuesta.EncuestaYVotacion.repository.*;
import com.encuesta.EncuestaYVotacion.web.dto.EncuestaDTO;
import com.encuesta.EncuestaYVotacion.web.dto.RespuestaSubmissionDTO;
import com.encuesta.EncuestaYVotacion.web.dto.EncuestaDetalleDTO;
import com.encuesta.EncuestaYVotacion.web.dto.RespuestaAdminDTO;
import com.encuesta.EncuestaYVotacion.web.dto.EncuestaResumenDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/encuestas")
public class EncuestaRestController {
    private final EncuestaRepository encuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RespuestaRepository respuestaRepository;
    private final PreguntaRepository preguntaRepository;
    private final OpcionEncuestaRepository opcionEncuestaRepository;

    public EncuestaRestController(EncuestaRepository encuestaRepository, UsuarioRepository usuarioRepository,
                                  RespuestaRepository respuestaRepository, PreguntaRepository preguntaRepository,
                                  OpcionEncuestaRepository opcionEncuestaRepository) {
        this.encuestaRepository = encuestaRepository;
        this.usuarioRepository = usuarioRepository;
        this.respuestaRepository = respuestaRepository;
        this.preguntaRepository = preguntaRepository;
        this.opcionEncuestaRepository = opcionEncuestaRepository;
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody EncuestaDTO dto){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        if (correo == null) {
            return ResponseEntity.status(401).build();
        }
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null){
            return ResponseEntity.status(401).build();
        }

        Encuesta enc = new Encuesta();
        enc.setTitulo(dto.titulo);
        enc.setDescripcion(dto.descripcion);
        enc.setTipoEncuesta(dto.tipoEncuesta);
        enc.setEstado("activa");
        enc.setEsVotacion(Boolean.TRUE.equals(dto.esVotacion));
        enc.setUsuario(usuario);

        if (dto.preguntas != null){
            for (EncuestaDTO.PreguntaDTO p : dto.preguntas){
                Pregunta pr = new Pregunta();
                pr.setEncuesta(enc);
                pr.setTexto(p.texto);
                pr.setTipo(normalizarTipo(p.tipo));

                if (p.opciones != null){
                    for (EncuestaDTO.OpcionDTO o : p.opciones){
                        OpcionEncuesta op = new OpcionEncuesta();
                        op.setPregunta(pr);
                        op.setTexto(o.texto);
                        pr.getOpciones().add(op);
                    }
                }
                enc.getPreguntas().add(pr);
            }
        }
        Encuesta guardada = encuestaRepository.save(enc);
        return ResponseEntity.created(URI.create("/api/encuestas/" + guardada.getId()))
                .body(guardada.getId());
    }

    @GetMapping
    public List<EncuestaResumenDTO> listar(
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "esVotacion", required = false) Boolean esVotacion){
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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        Usuario usuario = correo != null ? usuarioRepository.findByCorreo(correo).orElse(null) : null;

        List<EncuestaResumenDTO> out = new ArrayList<>();
        for (Encuesta e : list){
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        if (correo == null) return ResponseEntity.status(401).build();
        
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null || !"administrador".equals(usuario.getTipoUsuario())) {
            return ResponseEntity.status(403).body("Solo el administrador puede eliminar encuestas.");
        }

        if (!encuestaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Eliminar respuestas asociadas primero para evitar violaciones de FK
        List<Respuesta> respuestas = respuestaRepository.findByEncuestaId(id);
        if (!respuestas.isEmpty()) {
            respuestaRepository.deleteAll(respuestas);
        }

        encuestaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EncuestaDetalleDTO> obtenerDetalle(@PathVariable Integer id) {
        return encuestaRepository.findById(id)
                .map(this::mapToDetalleDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/participar")
    public ResponseEntity<?> participar(@PathVariable Integer id, @RequestBody RespuestaSubmissionDTO dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String correo = auth != null ? auth.getName() : null;
            if (correo == null) return ResponseEntity.status(401).body("Usuario no autenticado");
            Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
            if (usuario == null) return ResponseEntity.status(401).body("Usuario no encontrado");

            Encuesta encuesta = encuestaRepository.findById(id).orElse(null);
            if (encuesta == null) return ResponseEntity.notFound().build();

            if (dto.respuestas == null || dto.respuestas.isEmpty()) {
                 return ResponseEntity.badRequest().body("No se enviaron respuestas");
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
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/respuestas")
    public ResponseEntity<List<RespuestaAdminDTO>> verRespuestas(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        
        Encuesta encuesta = encuestaRepository.findById(id).orElse(null);
        if (encuesta == null) return ResponseEntity.notFound().build();
        
        if (usuario == null || (!"administrador".equals(usuario.getTipoUsuario()) && !encuesta.getUsuario().getId().equals(usuario.getId()))) {
             return ResponseEntity.status(403).build();
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
        return ResponseEntity.ok(out);
    }

    private EncuestaDetalleDTO mapToDetalleDTO(Encuesta e) {
        EncuestaDetalleDTO d = new EncuestaDetalleDTO();
        d.id = e.getId();
        d.titulo = e.getTitulo();
        d.descripcion = e.getDescripcion();
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
        return switch (t) {
            case "parrafo" -> "parrafo";
            case "opcion_multiple" -> "opcion_multiple";
            case "casillas" -> "casillas";
            case "desplegable" -> "desplegable";
            default -> "corta";
        };
    }
}

