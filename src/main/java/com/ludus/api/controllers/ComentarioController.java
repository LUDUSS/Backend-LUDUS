package com.ludus.api.controllers;

import com.ludus.api.models.Comentario;
import com.ludus.api.services.ComentarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/ludus/comentario/")
public class ComentarioController {
    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    @GetMapping("resenia/{id_resenia}")
    public List<Comentario> getComentariosPorResenia(@PathVariable Integer id_resenia) {
        return service.getComentariosPorResenia(id_resenia);
    }

    @PostMapping
    public Comentario addComentario(@RequestBody Comentario comentario) {
        return service.addComentario(comentario);
    }
}
