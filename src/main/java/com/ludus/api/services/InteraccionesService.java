package com.ludus.api.services;

import com.ludus.api.models.Interaccion;
import com.ludus.api.repositories.InteraccionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InteraccionesService {

    private final InteraccionRepository repository;

    public InteraccionesService(InteraccionRepository repository) {
        this.repository = repository;
    }

    public List<Interaccion> getInteracciones() {
        return repository.findAll();
    }

    public Interaccion getInteraccion(Integer id_interaccion) {
        return repository
                .findById(id_interaccion)
                .orElseThrow( () -> new IllegalArgumentException("La publicación con el id [" + id_interaccion + "] no existe."));
    } // getInteraccion

    public Interaccion addInteraccion(Interaccion interaccion) {
        // Validamos que las relaciones no vengan nulas
        if (interaccion.getUsuario() == null || interaccion.getUsuario().getId_usuario() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio para la interacción.");
        }
        if (interaccion.getPublicacion() == null || interaccion.getPublicacion().getId_resenia() == null) {
            throw new IllegalArgumentException("La publicación es obligatoria para la interacción.");
        }

        // Si no envían fecha, se asigna la actual
        if (interaccion.getFecha_like() == null) {
            interaccion.setFecha_like(LocalDateTime.now());
        }

        return repository.save(interaccion);
    }

    public Interaccion deleteInteraccion(Integer id_interaccion) {
        Interaccion interaccion = null;
        if (repository.existsById(id_interaccion)) {
            interaccion = repository.findById(id_interaccion).get();
            repository.delete(interaccion);
        }
        return interaccion;
    }

    public Interaccion toggleLike(Integer idUsuario, Integer idResenia) {
        if (repository.existsByIdUsuarioAndIdResenia(idUsuario, idResenia)) {
            Interaccion i = repository.findByIdUsuarioAndIdResenia(idUsuario, idResenia).get();
            repository.delete(i);
            return i;
        }
        Interaccion nuevo = new Interaccion();
        nuevo.setIdUsuario(idUsuario);
        nuevo.setIdResenia(idResenia);
        nuevo.setFecha_like(LocalDateTime.now());
        return repository.save(nuevo);
    }

    public long contarLikes(Integer idResenia) {
        return repository.findByIdResenia(idResenia).size();
    }

    public boolean yaLikeado(Integer idUsuario, Integer idResenia) {
        return repository.existsByIdUsuarioAndIdResenia(idUsuario, idResenia);
    }
}
