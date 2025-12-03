package com.encuesta.EncuestaYVotacion.web;

import com.encuesta.EncuestaYVotacion.service.VotacionService;
import com.encuesta.EncuestaYVotacion.web.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/votaciones")
public class VotacionRestController {

    private final VotacionService votacionService;

    public VotacionRestController(VotacionService votacionService) {
        this.votacionService = votacionService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> crear(@RequestBody EncuestaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        if (correo == null) return ResponseEntity.status(401).build();

        try {
            Integer id = votacionService.crear(dto, correo);
            return ResponseEntity.created(URI.create("/api/votaciones/" + id)).body(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<EncuestaResumenDTO> listar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        return votacionService.listar(correo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EncuestaDetalleDTO> obtener(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(votacionService.obtener(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/participar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> votar(@PathVariable Integer id, @RequestBody RespuestaSubmissionDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        if (correo == null) return ResponseEntity.status(401).build();

        try {
            votacionService.votar(id, dto, correo);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/respuestas")
    public ResponseEntity<List<RespuestaAdminDTO>> verResultados(@PathVariable Integer id) {
        return ResponseEntity.ok(votacionService.verResultados(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            votacionService.eliminar(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody EncuestaDTO dto) {
        try {
            votacionService.actualizar(id, dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
