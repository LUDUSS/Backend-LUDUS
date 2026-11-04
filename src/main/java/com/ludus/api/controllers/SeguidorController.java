package com.ludus.api.controllers;

import com.ludus.api.models.Seguidor;
import com.ludus.api.services.SeguidorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ludus/seguidor/")
public class SeguidorController {

    private final SeguidorService service;

    public SeguidorController(SeguidorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Seguidor>> getSeguidores() {
        return ResponseEntity.ok(service.getSeguidores());
    }

    @GetMapping("siguiendo/{id_usuario}")
    public ResponseEntity<List<Seguidor>> getSiguiendo(@PathVariable Integer id_usuario) {
        return ResponseEntity.ok(service.getSiguiendo(id_usuario));
    }

    @GetMapping("seguidores/{id_usuario}")
    public ResponseEntity<List<Seguidor>> getSeguidoresDe(@PathVariable Integer id_usuario) {
        return ResponseEntity.ok(service.getSeguidoresDe(id_usuario));
    }

    @PostMapping("seguir")
    public ResponseEntity<Seguidor> addSeguidor(@RequestParam Integer idSeguidor, @RequestParam Integer idSeguido) {
        Seguidor nuevoSeguimiento = service.addSeguidor(idSeguidor, idSeguido);
        return new ResponseEntity<>(nuevoSeguimiento, HttpStatus.CREATED);
    }

    @DeleteMapping("{id_seguimiento}")
    public ResponseEntity<Void> deleteSeguidor(@PathVariable Integer id_seguimiento) {
        service.deleteSeguidor(id_seguimiento);
        return ResponseEntity.noContent().build();
    }
}