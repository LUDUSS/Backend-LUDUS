package com.ludus.api.controllers;

import com.ludus.api.models.Juego;
import com.ludus.api.services.JuegoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping(path = "/ludus/juego/")
public class JuegoController {
    private final JuegoService service;

    @Autowired
    public JuegoController(JuegoService service) {
        this.service = service;
    }// constructor

    @GetMapping
    public List<Juego> getJuegos(){
        return service.getJuegos();
    }// getJuegos

    @GetMapping("{id_juego}")
    public Juego getJuego(@PathVariable Integer id_juego){
        return service.getJuego(id_juego);
    }// getJuego

    @DeleteMapping("{id_juego}")
    public Juego deleteJuego(@PathVariable Integer id_juego){
        return service.deleteJuego(id_juego);
    }// deleteJuego

    @PostMapping
    public Juego addJuego(@RequestBody Juego juego){
        return service.addJuego(juego);
    }

    @PutMapping("/{id_juego}")
    public Juego updateJuego(@PathVariable Integer id_juego, @RequestBody Juego juego){
        return service.updateJuego(
                id_juego,
                juego.getNombre_juego(),
                juego.getCategoria(),
                juego.getPlataforma(),
                juego.getDescripcion(),
                juego.getImagen_url()
        );
    }
}
// class JuegoController