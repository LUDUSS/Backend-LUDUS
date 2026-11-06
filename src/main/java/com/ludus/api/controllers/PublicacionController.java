package com.ludus.api.controllers;
import com.ludus.api.models.Publicacion;
import com.ludus.api.services.PublicacionService;
import com.ludus.api.security.CurrentUser;
import com.ludus.api.dtos.ReviewDtos.*;
import com.ludus.api.dtos.CatalogDtos.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/ludus/publicacion")
public class PublicacionController {
    private final PublicacionService service;
    private final CurrentUser current;
    public PublicacionController(PublicacionService service,CurrentUser current) {this.service=service;this.current=current;}
    @GetMapping({"","/"}) public Page<Review> all(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {return service.list(null,null,page,size);}
    @GetMapping("/{id}") public Review detail(@PathVariable Integer id) {return service.detail(id);}
    @PostMapping({"/",""}) @ResponseStatus(HttpStatus.CREATED)
    public Review legacy(@RequestBody Publicacion input) {return service.publishLocal(input);}
    @PostMapping("/igdb/{id}") @ResponseStatus(HttpStatus.CREATED)
    public Review publish(@PathVariable Long id,@RequestBody Input body) {return service.publishIgdb(id,body);}
    @GetMapping("/juego/igdb/{id}") public Page<Review> game(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {return service.list(id,null,page,size);}
    @GetMapping("/juego/igdb/{id}/rating") public Social rating(@PathVariable Long id) {return service.social(id);}
    @GetMapping("/me") public Page<Review> mine(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {return service.list(null,current.get().getId_usuario(),page,size);}
    @GetMapping("/usuario/{id}") public Page<Review> profile(@PathVariable Integer id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {return service.list(null,id,page,size);}
    @PutMapping("/{id}") public Review update(@PathVariable Integer id,@RequestBody(required=false) Input body,
        @RequestParam(required=false) Double calificacion,@RequestParam(required=false) String texto_resenia) {
        return service.update(id,body==null?new Input(texto_resenia,calificacion):body);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Integer id) {service.delete(id);}
}
