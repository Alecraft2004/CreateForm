package com.encuesta.EncuestaYVotacion.web;

import com.encuesta.EncuestaYVotacion.model.*;
import com.encuesta.EncuestaYVotacion.repository.EncuestaRepository;
import com.encuesta.EncuestaYVotacion.repository.UsuarioRepository;
import com.encuesta.EncuestaYVotacion.web.dto.EncuestaDTO;
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

    public EncuestaRestController(EncuestaRepository encuestaRepository, UsuarioRepository usuarioRepository) {
        this.encuestaRepository = encuestaRepository;
        this.usuarioRepository = usuarioRepository;
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
        enc.setEstado("activa");
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
    public List<EncuestaResumenDTO> listar(@RequestParam(name = "estado", required = false) String estado){
        List<Encuesta> list = (estado == null || estado.isBlank()) ? encuestaRepository.findAll() : encuestaRepository.findByEstado(estado);
        List<EncuestaResumenDTO> out = new ArrayList<>();
        for (Encuesta e : list){
            EncuestaResumenDTO r = new EncuestaResumenDTO();
            r.id = e.getId();
            r.titulo = e.getTitulo();
            r.descripcion = e.getDescripcion();
            r.estado = e.getEstado();
            r.preguntas = e.getPreguntas() != null ? e.getPreguntas().size() : 0;
            out.add(r);
        }
        return out;
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

class EncuestaResumenDTO {
    public Integer id;
    public String titulo;
    public String descripcion;
    public String estado;
    public int preguntas;
}
