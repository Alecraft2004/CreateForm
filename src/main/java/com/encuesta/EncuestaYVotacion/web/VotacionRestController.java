package com.encuesta.EncuestaYVotacion.web;

import com.encuesta.EncuestaYVotacion.model.*;
import com.encuesta.EncuestaYVotacion.repository.*;
import com.encuesta.EncuestaYVotacion.web.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/votaciones")
public class VotacionRestController {

    private final VotacionRepository votacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final VotoRepository votoRepository;
    private final OpcionVotacionRepository opcionVotacionRepository;

    public VotacionRestController(VotacionRepository votacionRepository, UsuarioRepository usuarioRepository,
                                  VotoRepository votoRepository, OpcionVotacionRepository opcionVotacionRepository) {
        this.votacionRepository = votacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.votoRepository = votoRepository;
        this.opcionVotacionRepository = opcionVotacionRepository;
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody EncuestaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        if (correo == null) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) return ResponseEntity.status(401).build();

        Votacion v = new Votacion();
        v.setTitulo(dto.titulo);
        v.setEstado("activa");
        v.setUsuario(usuario);

        // Mapear preguntas[0].opciones a OpcionVotacion
        // Asumimos que la votación simple tiene 1 pregunta conceptual con opciones
        if (dto.preguntas != null && !dto.preguntas.isEmpty()) {
            EncuestaDTO.PreguntaDTO p = dto.preguntas.get(0);
            v.setTipo(p.tipo); // Guardar el tipo de pregunta
            if (p.opciones != null) {
                for (EncuestaDTO.OpcionDTO o : p.opciones) {
                    OpcionVotacion ov = new OpcionVotacion();
                    ov.setTexto(o.texto);
                    ov.setVotacion(v);
                    v.getOpciones().add(ov);
                }
            }
        }

        Votacion guardada = votacionRepository.save(v);
        return ResponseEntity.created(URI.create("/api/votaciones/" + guardada.getId()))
                .body(guardada.getId());
    }

    @GetMapping
    public List<EncuestaResumenDTO> listar() {
        List<Votacion> list = votacionRepository.findAll();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        Usuario usuario = correo != null ? usuarioRepository.findByCorreo(correo).orElse(null) : null;

        List<EncuestaResumenDTO> out = new ArrayList<>();
        for (Votacion v : list) {
            EncuestaResumenDTO r = new EncuestaResumenDTO();
            r.id = v.getId();
            r.titulo = v.getTitulo();
            r.descripcion = "Votación"; // Votacion entity doesn't have description in schema provided
            r.tipoEncuesta = "votacion";
            r.estado = v.getEstado();
            r.preguntas = 1; // Conceptual
            
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

    @GetMapping("/{id}")
    public ResponseEntity<EncuestaDetalleDTO> obtener(@PathVariable Integer id) {
        return votacionRepository.findById(id).map(v -> {
            EncuestaDetalleDTO d = new EncuestaDetalleDTO();
            d.id = v.getId();
            d.titulo = v.getTitulo();
            d.descripcion = "Votación";
            d.tipoEncuesta = "votacion";
            
            EncuestaDetalleDTO.PreguntaDetalleDTO p = new EncuestaDetalleDTO.PreguntaDetalleDTO();
            p.id = 1; // Dummy ID
            p.texto = v.getTitulo(); // Use title as question text
            p.tipo = v.getTipo() != null ? v.getTipo() : "casillas"; // Use stored type or default
            p.opciones = new ArrayList<>();
            
            for (OpcionVotacion ov : v.getOpciones()) {
                EncuestaDetalleDTO.OpcionDetalleDTO od = new EncuestaDetalleDTO.OpcionDetalleDTO();
                od.id = ov.getId();
                od.texto = ov.getTexto();
                p.opciones.add(od);
            }
            d.preguntas = Collections.singletonList(p);
            return d;
        }).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/participar")
    public ResponseEntity<?> votar(@PathVariable Integer id, @RequestBody RespuestaSubmissionDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        if (correo == null) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        
        Votacion v = votacionRepository.findById(id).orElse(null);
        if (v == null) return ResponseEntity.notFound().build();

        // Check if already voted
        if (votoRepository.countByVotacionIdAndUsuarioId(id, usuario.getId()) > 0) {
            return ResponseEntity.badRequest().body("Ya has votado en esta votación.");
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
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/respuestas")
    public ResponseEntity<List<RespuestaAdminDTO>> verResultados(@PathVariable Integer id) {
        // Reuse RespuestaAdminDTO for consistency
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
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        
        if (usuario == null || !"administrador".equals(usuario.getTipoUsuario())) {
             return ResponseEntity.status(403).build();
        }
        if (votacionRepository.existsById(id)) {
            votacionRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody EncuestaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        
        if (usuario == null || !"administrador".equals(usuario.getTipoUsuario())) {
             return ResponseEntity.status(403).build();
        }

        Votacion v = votacionRepository.findById(id).orElse(null);
        if (v == null) return ResponseEntity.notFound().build();

        v.setTitulo(dto.titulo);
        
        // Eliminar votos existentes para evitar violaciones de FK al modificar opciones
        List<Voto> votos = votoRepository.findByVotacionId(id);
        if (!votos.isEmpty()) {
            votoRepository.deleteAll(votos);
        }
        
        v.getOpciones().clear();
        
        if (dto.preguntas != null && !dto.preguntas.isEmpty()) {
            EncuestaDTO.PreguntaDTO p = dto.preguntas.get(0);
            v.setTipo(p.tipo); // Actualizar el tipo
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
        return ResponseEntity.ok().build();
    }
}
