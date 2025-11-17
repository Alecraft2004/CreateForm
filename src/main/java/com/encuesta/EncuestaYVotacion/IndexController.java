package com.encuesta.EncuestaYVotacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String home(Model model) {
        model.addAttribute("mensaje", "CreateForm");
        return "index";
    }
    
    @GetMapping("/encuestas")
    public String verEncuestas() {
        return "encuestas";
    }

    @GetMapping("/votaciones")
    public String verVotaciones() {
        return "votaciones";
    }

    
     @GetMapping("/crear-encuesta")
    public String crearEncuesta() {
        return "crear-encuesta";
    }
     @GetMapping("/crear-votacion")
    public String crearVotacion() {
        return "crear-votacion";
    }
}
