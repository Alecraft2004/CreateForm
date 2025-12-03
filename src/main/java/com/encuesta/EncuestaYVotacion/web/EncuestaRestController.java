package com.encuesta.EncuestaYVotacion.web;

import com.encuesta.EncuestaYVotacion.service.EncuestaService;
import com.encuesta.EncuestaYVotacion.web.dto.EncuestaDTO;
import com.encuesta.EncuestaYVotacion.web.dto.RespuestaSubmissionDTO;
import com.encuesta.EncuestaYVotacion.web.dto.EncuestaDetalleDTO;
import com.encuesta.EncuestaYVotacion.web.dto.RespuestaAdminDTO;
import com.encuesta.EncuestaYVotacion.web.dto.EncuestaResumenDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/encuestas")
public class EncuestaRestController {

    private final EncuestaService encuestaService;

    public EncuestaRestController(EncuestaService encuestaService) {
        this.encuestaService = encuestaService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> crear(@RequestBody EncuestaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        if (correo == null) return ResponseEntity.status(401).build();

        try {
            Integer id = encuestaService.crear(dto, correo);
            return ResponseEntity.created(URI.create("/api/encuestas/" + id)).body(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<EncuestaResumenDTO> listar(
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "esVotacion", required = false) Boolean esVotacion) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        return encuestaService.listar(estado, esVotacion, correo);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            encuestaService.eliminar(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody EncuestaDTO dto) {
        try {
            encuestaService.actualizar(id, dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EncuestaDetalleDTO> obtenerDetalle(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(encuestaService.obtenerDetalle(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/participar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> participar(@PathVariable Integer id, @RequestBody RespuestaSubmissionDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        if (correo == null) return ResponseEntity.status(401).build();

        try {
            encuestaService.participar(id, dto, correo);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno");
        }
    }

    @GetMapping("/{id}/respuestas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> verRespuestas(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        if (correo == null) return ResponseEntity.status(401).build();

        try {
            return ResponseEntity.ok(encuestaService.verRespuestas(id, correo));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("No autorizado")) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.notFound().build();
        }
    }
}

