package com.ludus.api.controllers;

import com.ludus.api.models.Seguidor;
import com.ludus.api.services.SeguidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/ludus/seguidor/")
@CrossOrigin(origins = "*")
public class SeguidorController {

    private final SeguidorService service;

    @Autowired
    public SeguidorController(SeguidorService service) {
        this.service = service;
    }

    // Ver todos los registros de seguimiento
    @GetMapping
    public List<Seguidor> getSeguidores() {
        return service.getSeguidores();
    }

    // A quiénes sigue un usuario (siguiendo)
    @GetMapping("siguiendo/{id_usuario}")
    public List<Seguidor> getSiguiendo(@PathVariable Integer id_usuario) {
        return service.getSiguiendo(id_usuario);
    }

    // Quiénes siguen a un usuario (seguidores)
    @GetMapping("seguidores/{id_usuario}")
    public List<Seguidor> getSeguidoresDe(@PathVariable Integer id_usuario) {
        return service.getSeguidoresDe(id_usuario);
    }

    // Seguir a un usuario
    @PostMapping("seguir")
    public Seguidor addSeguidor(@RequestParam Integer idSeguidor, @RequestParam Integer idSeguido) {
        return service.addSeguidor(idSeguidor, idSeguido);
    }

    // Dejar de seguir por ID de seguimiento
    @DeleteMapping("{id_seguimiento}")
    public Seguidor deleteSeguidor(@PathVariable Integer idSeguimiento) {
        return service.deleteSeguidor(idSeguimiento);
    }

    // Dejar de seguir por pareja (seguidor y seguido)
    @DeleteMapping("dejar")
    public Seguidor dejarDeSeguir(@RequestParam Integer idSeguidor, @RequestParam Integer idSeguido) {
        return service.deleteSeguidor(idSeguidor, idSeguido);
    }
}
