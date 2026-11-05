package com.ludus.api.services;

import com.ludus.api.models.Interaccion;
import com.ludus.api.repositories.InteraccionRepository;
import org.springframework.stereotype.Service;

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
        return repository.findById(id_interaccion)
                .orElseThrow(() -> new IllegalArgumentException("La interacción con el ID [" + id_interaccion + "] no existe."));
    }

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

    public void deleteInteraccion(Integer id_interaccion) {
        if (!repository.existsById(id_interaccion)) {
            throw new IllegalArgumentException("La interacción con el ID [" + id_interaccion + "] no existe.");
        }
        repository.deleteById(id_interaccion);
    }
}