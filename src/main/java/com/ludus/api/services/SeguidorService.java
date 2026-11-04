package com.ludus.api.services;

import com.ludus.api.models.Seguidor;
import com.ludus.api.repositories.SeguidorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeguidorService {

    private final SeguidorRepository repository;

    public SeguidorService(SeguidorRepository repository) {
        this.repository = repository;
    }// constructor

    public List<Seguidor> getSeguidores() {
        return repository.findAll();
    }// getSeguidores

    public List<Seguidor> getSiguiendo(Integer idUsuario) {
        return repository.findByIdSeguidor(idUsuario);
    }// getFollowing

    public List<Seguidor> getSeguidoresDe(Integer idUsuario) {
        return repository.findByIdSeguido(idUsuario);
    }// getFollowers

    public Seguidor addSeguidor(Integer idSeguidor, Integer idSeguido) {
        // No seguirse a sí mismo
        if (idSeguidor.equals(idSeguido)) {
            throw new IllegalArgumentException("Un usuario no puede seguirse a sí mismo");
        }
        // Evitar duplicados
        if (repository.existsByIdSeguidorAndIdSeguido(idSeguidor, idSeguido)) {
            throw new IllegalArgumentException("El usuario " + idSeguidor + " ya sigue al usuario " + idSeguido);
        }

        // Temporal: no validamos que los usuarios existan (eso vendrá con @ManyToOne)
        Seguidor nuevo = new Seguidor(idSeguidor, idSeguido, LocalDateTime.now());
        return repository.save(nuevo);
    }// addSeguidor

    public Seguidor deleteSeguidor(Integer idSeguimiento) {
        Seguidor seguidor = null;
        if (repository.existsById(idSeguimiento)) {
            seguidor = repository.findById(idSeguimiento).get();
            repository.delete(seguidor);
        }// if
        return seguidor;
    }// deleteSeguidor
}// class SeguidorService
