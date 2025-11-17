package com.encuesta.EncuestaYVotacion.web;

import com.encuesta.EncuestaYVotacion.model.Usuario;
import com.encuesta.EncuestaYVotacion.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    private final UsuarioRepository usuarioRepository;

    public DebugController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/usuarios")
    public List<Map<String, Object>> usuarios(){
        List<Map<String, Object>> out = new ArrayList<>();
        for (Usuario u : usuarioRepository.findAll()){
            Map<String,Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nombre", u.getNombre());
            m.put("correo", u.getCorreo());
            m.put("tipoUsuario", u.getTipoUsuario());
            out.add(m);
        }
        return out;
    }
}

