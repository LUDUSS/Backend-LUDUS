package com.ludus.api.controllers;
import com.ludus.api.services.*;
import com.ludus.api.models.Juego;
import com.ludus.api.dtos.CatalogDtos.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ludus/igdb")
public class IgdbController {
    private final IgdbService service;
    private final GameResolver resolver;
    private final com.ludus.api.security.CurrentUser current;
    public IgdbController(IgdbService service,GameResolver resolver,com.ludus.api.security.CurrentUser current) {this.service=service;this.resolver=resolver;this.current=current;}
    @GetMapping("/buscar")
    public Page<Game> buscar(@RequestParam String nombre,@RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size,@RequestParam(defaultValue="relevancia") String sort,
            @RequestParam(required=false) Long genreId,@RequestParam(required=false) Long platformId) {
        return service.buscarJuegos(nombre,page,size,sort,genreId,platformId);
    }
    @GetMapping("/juegos")
    public Page<Game> explorar(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size,
            @RequestParam(defaultValue="rating") String sort,@RequestParam(required=false) Long genreId,
            @RequestParam(required=false) Long platformId) {return service.explorar(page,size,sort,genreId,platformId);}
    @GetMapping("/juegos/{id}") public Game detalle(@PathVariable Long id) {return service.detalle(id);}
    @PostMapping("/juegos/{id}/resolver") public Juego resolver(@PathVariable Long id) {current.get();return resolver.resolve(id);}
    @GetMapping("/generos") public Page<Lookup> generos(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {return service.lookup("/genres",page,size);}
    @GetMapping("/plataformas") public Page<Lookup> plataformas(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {return service.lookup("/platforms",page,size);}
}
