package com.ludus.api.services;

import com.ludus.api.models.Juego;
import com.ludus.api.models.Publicacion;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.JuegoRepository;
import com.ludus.api.repositories.PublicacionRepository;
import com.ludus.api.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PublicacionService {
    // conectar PublicacionRepository
    private final PublicacionRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final JuegoRepository juegoRepository;

    // CONSTRUCTOR DE PublicacionService
    public PublicacionService(
            PublicacionRepository repository,
            UsuarioRepository usuarioRepository,
            JuegoRepository juegoRepository) {

        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.juegoRepository = juegoRepository;
    }// fin constructor

    public List<Publicacion> getPublicacion(){
        return  repository.findAll(); // -> QUERY (SELECT * FROM Publicacion)
    }

    public Publicacion getPublicacion(Integer id_resenia) {
        return repository
                .findById(id_resenia)
                .orElseThrow(() -> new IllegalArgumentException("La publicacion con el ID ["+ id_resenia +"] no existe"));
    }// fin getPublicacion (id)

    public Publicacion deletePublicacion (Integer id_resenia){
        Publicacion publicacion = null;
        if(repository.existsById(id_resenia)){// buscar la publicacaion or id
            publicacion = repository.findById(id_resenia).get();
            repository.delete(publicacion);
        }// fin if
        return publicacion;
    }// fin deletePublicacion

    // Crear una nueva resenia
    public Publicacion addPublicacion(Publicacion publicacion) {

        // 1. Verificar que se enviaron las relaciones
        if (publicacion.getUsuario() == null ||
                publicacion.getJuego() == null ||
                publicacion.getUsuario().getId_usuario() == null ||
                publicacion.getJuego().getId_juego() == null) {

            throw new IllegalArgumentException(
                    "Debes indicar un usuario y un videojuego válidos."
            );
        }

        // 2. Buscar al usuario en la base de datos
        Integer idUsuario = publicacion.getUsuario().getId_usuario();

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario con ID [" + idUsuario + "] no existe."
                ));

        // 3. Buscar el videojuego en la base de datos
        Integer idJuego = publicacion.getJuego().getId_juego();

        Juego juego = juegoRepository.findById(idJuego)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El juego con ID [" + idJuego + "] no existe."
                ));

        // 4. Asignar las entidades encontradas
        publicacion.setUsuario(usuario);
        publicacion.setJuego(juego);

        // 5. Validar el contenido de la reseña
        if (publicacion.getTexto_resenia() == null ||
                publicacion.getTexto_resenia().isBlank() ||
                publicacion.getCalificacion() == null) {

            throw new IllegalArgumentException(
                    "La reseña debe contener texto y calificación."
            );
        }

        // 6. Validar que la calificación esté entre 1 y 5
        if (!Double.isFinite(publicacion.getCalificacion()) ||
                publicacion.getCalificacion() < 1 ||
                publicacion.getCalificacion() > 5) {

            throw new IllegalArgumentException(
                    "La calificación debe estar entre 1 y 5 estrellas."
            );
        }

        // 7. Asignar la fecha de publicación
        publicacion.setFecha_publicacion(LocalDateTime.now());

        // 8. Guardar la reseña
        return repository.save(publicacion);
    }

    public Publicacion updatePublicacion(Integer id_resenia, Double calificacion, String textoResenia) {
        Publicacion publicacion = null;
        // encontrar la publiccaion con el id
        if(repository.existsById(id_resenia)){
            Publicacion p = repository.findById(id_resenia).get();
            if(calificacion != null) p.setCalificacion(calificacion);
            if(textoResenia != null && !textoResenia.isBlank()) p.setTexto_resenia(textoResenia);
            publicacion = p;
            repository.save(publicacion);
        }
        return publicacion;
    }
}// fin class PublicacionService
