package com.ludus.api.services;

import com.ludus.api.models.Interaccion;
import com.ludus.api.models.Publicacion;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.InteraccionRepository;
import com.ludus.api.repositories.PublicacionRepository;
import com.ludus.api.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InteraccionesService {

    private final InteraccionRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;

    public InteraccionesService(InteraccionRepository repository,
                                UsuarioRepository usuarioRepository,
                                PublicacionRepository publicacionRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
    }

    public List<Interaccion> getInteracciones() {
        return repository.findAll();
    }

    public Interaccion getInteraccion(Integer id_interaccion) {
        return repository
                .findById(id_interaccion)
                .orElseThrow(() -> new IllegalArgumentException("La publicación con el id [" + id_interaccion + "] no existe."));
    }

    public Interaccion addInteraccion(Interaccion interaccion) {
        if (interaccion.getUsuario() == null || interaccion.getUsuario().getId_usuario() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio para la interacción.");
        }
        if (interaccion.getPublicacion() == null || interaccion.getPublicacion().getId_resenia() == null) {
            throw new IllegalArgumentException("La publicación es obligatoria para la interacción.");
        }

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
        if (repository.existsByUsuarioIdAndPublicacionId(idUsuario, idResenia)) {
            Interaccion i = repository.findByUsuarioIdAndPublicacionId(idUsuario, idResenia).orElse(null);
            if (i != null) {
                repository.delete(i);
            }
            return i;
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario));
        Publicacion publicacion = publicacionRepository.findById(idResenia)
                .orElseThrow(() -> new IllegalArgumentException("Publicación/Reseña no encontrada con ID: " + idResenia));

        Interaccion nuevo = new Interaccion();
        nuevo.setUsuario(usuario);
        nuevo.setPublicacion(publicacion);
        nuevo.setFecha_like(LocalDateTime.now());
        return repository.save(nuevo);
    }

    public long contarLikes(Integer idResenia) {
        return repository.findByPublicacionId(idResenia).size();
    }

    public boolean yaLikeado(Integer idUsuario, Integer idResenia) {
        return repository.existsByUsuarioIdAndPublicacionId(idUsuario, idResenia);
    }
}
