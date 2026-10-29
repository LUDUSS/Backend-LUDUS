package com.ludus.api.controllers;


import com.ludus.api.models.Interaccion;
import com.ludus.api.services.InteraccionesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/ludus/interaccion/")
public class InteraccionController {

    private final InteraccionesService service;

    @Autowired
    public InteraccionController(InteraccionesService service) {
        this.service = service;
    } // Constructor

    @GetMapping
    public List<Interaccion> getInteracciones() {
        return service.getInteracciones();
    } // getInteracciones

    @GetMapping("/{id_interaccion}")
    public Interaccion getInteraccion(@PathVariable Integer id_interaccion){
        return  service.getInteraccion(id_interaccion);
    } // getInteraccion


    @PostMapping
    public Interaccion addInteraccion(@RequestBody Interaccion interaccion) {
        return service.addInteraccion(interaccion);
    } // addInteraccion

    @DeleteMapping("/{id_interaccion}")
    public Interaccion deleteInteraccion(@PathVariable Integer id_interaccion) {
        return service.deleteInteraccion(id_interaccion);
    }

} // class InteraccionController
