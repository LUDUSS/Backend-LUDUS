package com.ludus.api.services;

import com.ludus.api.models.Interaccion;
import com.ludus.api.repositories.InteraccionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InteraccionesService {

    private final InteraccionRepository repository;

    public InteraccionesService(InteraccionRepository repository) {
        this.repository = repository;
    }// constructor

    public List<Interaccion> getInteracciones() {
        return repository.findAll();
    } // getInteracciones

    public Interaccion getInteraccion(Integer id_interaccion) {
        return repository
                .findById(id_interaccion)
                .orElseThrow( () -> new IllegalArgumentException("La publicación con el id [" + id_interaccion + "] no existe."));
    } // getInteraccion

    public Interaccion addInteraccion(Interaccion interaccion) {
        if (interaccion.getFecha_like() != null) { // No olvidar guardar los parametros faltantes cuando se agregen las FK
            return repository.save(interaccion);
        }
        throw new IllegalArgumentException("La fecha de la interacción no puede ser nula");
    }// addInteraccion

    public Interaccion deleteInteraccion(Integer id_interaccion) {
        Interaccion interaccion = null;

        if(repository.existsById(id_interaccion)){
            interaccion = repository.findById(id_interaccion).get();
            repository.delete(interaccion);
        }// if

        return interaccion;
    } // deleteInteraccion
} // class InteraccionesService
