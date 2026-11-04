package com.ludus.api.controllers;

import com.ludus.api.models.Seguidor;
import com.ludus.api.services.SeguidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/ludus/seguidor/")
public class SeguidorController {

    private final SeguidorService service;

    @Autowired
    public SeguidorController(SeguidorService service) {
        this.service = service;
    }// constructor

    //ver todos los registros de seguimiento
    @GetMapping
    public List<Seguidor> getSeguidores() {
        return service.getSeguidores();
    }

    //siguiendo
    @GetMapping("siguiendo/{id_usuario}")
    public List<Seguidor> getSiguiendo(@PathVariable Integer id_usuario) {
        return service.getSiguiendo(id_usuario);
    }

    //seguidores
    @GetMapping("seguidores/{id_usuario}")
    public List<Seguidor> getSeguidoresDe (@PathVariable Integer id_usuario) {
        return service.getSeguidoresDe(id_usuario);
    }

    //seguir
    @PostMapping("seguir")
    public Seguidor addSeguidor(@RequestParam Integer idSeguidor, @RequestParam Integer idSeguido) {
        return service.addSeguidor(idSeguidor, idSeguido);
    }

    //dejar de seguir
    @DeleteMapping("{id_seguimiento}")
    public Seguidor deleteSeguidor(@PathVariable Integer id_seguimiento) {
        return service.deleteSeguidor(id_seguimiento);
    }
}// class SeguidorController
