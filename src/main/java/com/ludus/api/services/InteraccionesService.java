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

    public InteraccionesService(InteraccionRepository repository, UsuarioRepository usuarioRepository, PublicacionRepository publicacionRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
    }// constructor

    public List<Interaccion> getInteracciones() {
        return repository.findAll();
    }// getInteracciones

    public Interaccion getInteraccion(Integer id_interaccion) {
        return repository
                .findById(id_interaccion)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La interacción con el ID [" + id_interaccion + "] no existe."
                ));
    }// getInteraccion

    public Interaccion deleteInteraccion(Integer id_interaccion) {

        Interaccion interaccion = repository.findById(id_interaccion)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La interacción con ID [" + id_interaccion + "] no existe."
                ));

        repository.delete(interaccion);

        return interaccion;
    }// deleteInteraccion

    public Interaccion addInteraccion(Interaccion interaccion) {

        // 1. Verificar que se enviaron las relaciones
        if (interaccion.getUsuario() == null ||
                interaccion.getPublicacion() == null ||
                interaccion.getUsuario().getId_usuario() == null ||
                interaccion.getPublicacion().getId_resenia() == null) {

            throw new IllegalArgumentException(
                    "Debes indicar un usuario y una reseña válidos."
            );
        }

        // 2. Obtener los identificadores
        Integer idUsuario = interaccion.getUsuario().getId_usuario();
        Integer idResenia = interaccion.getPublicacion().getId_resenia();

        // 3. Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario con ID [" + idUsuario + "] no existe."
                ));

        // 4. Verificar que la reseña existe
        Publicacion publicacion = publicacionRepository.findById(idResenia)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La reseña con ID [" + idResenia + "] no existe."
                ));

        // 5. Evitar likes duplicados
        if (repository.existeLike(idUsuario, idResenia)) {
            throw new IllegalArgumentException(
                    "El usuario ya ha dado like a esta reseña."
            );
        }

        // 6. Asignar las entidades encontradas
        interaccion.setUsuario(usuario);
        interaccion.setPublicacion(publicacion);

        // 7. Generar la fecha automáticamente
        interaccion.setFecha_like(LocalDateTime.now());

        // 8. Guardar la interacción
        return repository.save(interaccion);
    }// addInteraccion
} // class InteraccionesService
