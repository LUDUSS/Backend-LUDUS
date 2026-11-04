package com.ludus.api.services;

import com.ludus.api.models.Comentario;
import com.ludus.api.models.Publicacion;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.ComentarioRepository;
import com.ludus.api.repositories.PublicacionRepository;
import com.ludus.api.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComentarioService {
    private final ComentarioRepository repository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(ComentarioRepository repository, PublicacionRepository publicacionRepository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
    }// constructor

    public List<Comentario> getComentariosPorResenia(Integer id_resenia) {
        return repository.findComentariosPorResenia(id_resenia);
    }// getComentariosPorResenia

    public Comentario addComentario(Comentario comentario) {

        // 1. Verificar que se enviaron las relaciones
        if (comentario.getPublicacion() == null ||
                comentario.getUsuario() == null ||
                comentario.getPublicacion().getId_resenia() == null ||
                comentario.getUsuario().getId_usuario() == null) {

            throw new IllegalArgumentException(
                    "Debes indicar una reseña y un usuario válidos."
            );
        }

        // 2. Buscar la reseña en la base de datos
        Integer idResenia = comentario.getPublicacion().getId_resenia();

        Publicacion publicacion = publicacionRepository.findById(idResenia)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La reseña con ID [" + idResenia + "] no existe."
                ));

        // 3. Buscar el usuario en la base de datos
        Integer idUsuario = comentario.getUsuario().getId_usuario();

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario con ID [" + idUsuario + "] no existe."
                ));

        // 4. Asignar las entidades encontradas
        comentario.setPublicacion(publicacion);
        comentario.setUsuario(usuario);

        // 5. Validar el contenido del comentario
        if (comentario.getTexto_comentario() == null ||
                comentario.getTexto_comentario().isBlank()) {

            throw new IllegalArgumentException(
                    "El comentario no puede estar vacío."
            );
        }

        // 6. Limpiar espacios al inicio y al final
        comentario.setTexto_comentario(
                comentario.getTexto_comentario().trim()
        );

        // 7. Asignar la fecha automáticamente
        comentario.setFecha_comentario(LocalDateTime.now());

        // 8. Guardar el comentario
        return repository.save(comentario);
    }
}
