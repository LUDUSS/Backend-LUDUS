package com.ludus.api.controllers;

import com.ludus.api.models.Publicacion;
import com.ludus.api.services.PublicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping (path = "/ludus/publicacion/")

public class PublicacionController {
    private final PublicacionService service;
    @Autowired// -> Gestiona la inyección de dependencias de forma automatica
    // -> INICIO CONSTRUCTOR PARA  PublicacionController
    public PublicacionController(PublicacionService service){
    this.service = service;
}// fin constructor

    @GetMapping
    public List<Publicacion> getPublicacion(){
        return  service.getPublicacion();
    }// fin LIST getPublicacion

    // SEGUNDO METODO GET HTTP
    @GetMapping("{id_resenia}")
    // Metodo Publico
    public Publicacion getPublicacion(@PathVariable Integer id_resenia){
        return service.getPublicacion(id_resenia);
    }// fin getPublicacion

    // borrar una publicacion con metodo delete
    @DeleteMapping("{id_resenia}")
    public  Publicacion deletePublicacion(@PathVariable Integer id_resenia){
        return service.deletePublicacion(id_resenia);
    }// fin deletePublicacion

    // agregar una publicacion
    @PostMapping
    public  Publicacion addPublicacion(@RequestBody Publicacion publicacion){ // -> Solicita los datos de la publicacion en el cuerpo de la solicitud en formato JSON
        return  service.addPublicacion(publicacion);
    }// fin addPublicacion

    // editar una publicacion
    @PutMapping
    public Publicacion updatePublicacion(@PathVariable Integer id_resenia,
                                         @RequestBody(required = false)Double calificacion,
                                         @RequestBody(required = false)String texto_resenia){
        return service.updatePublicacion(id_resenia,calificacion,texto_resenia);
    }// fin updatePublicacion
}// fin class PublicacionController
