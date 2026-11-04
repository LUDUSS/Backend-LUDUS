package com.ludus.api.controllers;

import com.ludus.api.models.Interaccion;
import com.ludus.api.services.InteraccionesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ludus/interaccion/")
public class InteraccionController {

    private final InteraccionesService service;

    public InteraccionController(InteraccionesService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Interaccion>> getInteracciones() {
        return ResponseEntity.ok(service.getInteracciones());
    }

    @GetMapping("/{id_interaccion}")
    public ResponseEntity<Interaccion> getInteraccion(@PathVariable Integer id_interaccion){
        return ResponseEntity.ok(service.getInteraccion(id_interaccion));
    }

    @PostMapping
    public ResponseEntity<Interaccion> addInteraccion(@RequestBody Interaccion interaccion) {
        Interaccion nuevaInteraccion = service.addInteraccion(interaccion);
        return new ResponseEntity<>(nuevaInteraccion, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id_interaccion}")
    public ResponseEntity<Void> deleteInteraccion(@PathVariable Integer id_interaccion) {
        service.deleteInteraccion(id_interaccion);
        return ResponseEntity.noContent().build();
    }
}