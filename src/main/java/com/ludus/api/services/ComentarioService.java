package com.ludus.api.services;

import com.ludus.api.models.Comentario;
import com.ludus.api.repositories.ComentarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComentarioService {
    private final ComentarioRepository repository;

    public ComentarioService(ComentarioRepository repository) {
        this.repository = repository;
    }

    public List<Comentario> getComentariosPorResenia(Integer id_resenia) {
        return repository.findComentariosPorResenia(id_resenia);
    }

    public Comentario addComentario(Comentario comentario) {
        if (comentario.getId_resenia() == null || comentario.getId_usuario() == null) {
            throw new IllegalArgumentException("La reseña y el usuario son obligatorios.");
        }
        if (comentario.getTexto_comentario() == null || comentario.getTexto_comentario().isBlank()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío.");
        }

        comentario.setTexto_comentario(comentario.getTexto_comentario().trim());
        comentario.setFecha_comentario(LocalDateTime.now());
        return repository.save(comentario);
    }
}
