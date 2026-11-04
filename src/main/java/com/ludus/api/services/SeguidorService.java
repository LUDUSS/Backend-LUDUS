package com.ludus.api.services;

import com.ludus.api.models.Seguidor;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.SeguidorRepository;
import com.ludus.api.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeguidorService {

    private final SeguidorRepository repository;
    private final UsuarioRepository usuarioRepository;

    public SeguidorService(SeguidorRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }// constructor

    public List<Seguidor> getSeguidores() {
        return repository.findAll();
    }// getSeguidores

    public List<Seguidor> getSiguiendo(Integer idUsuario) {
        return repository.findSiguiendo(idUsuario);
    }// getSiguiendo

    public List<Seguidor> getSeguidoresDe(Integer idUsuario) {
        return repository.findSeguidores(idUsuario);
    }// getFollowers

    public Seguidor deleteSeguidor(Integer idSeguimiento) {

        Seguidor seguimiento = repository.findById(idSeguimiento)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El seguimiento con ID [" + idSeguimiento + "] no existe."
                ));

        repository.delete(seguimiento);

        return seguimiento;
    }// deleteSeguidor

    public Seguidor addSeguidor(Integer idSeguidor, Integer idSeguido) {

        // 1. Validar que se recibieron los identificadores
        if (idSeguidor == null || idSeguido == null) {
            throw new IllegalArgumentException(
                    "Debes indicar ambos usuarios."
            );
        }

        // 2. Evitar que un usuario se siga a sí mismo
        if (idSeguidor.equals(idSeguido)) {
            throw new IllegalArgumentException(
                    "Un usuario no puede seguirse a sí mismo."
            );
        }

        // 3. Buscar al usuario que realiza el seguimiento
        Usuario seguidor = usuarioRepository.findById(idSeguidor)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario con ID [" + idSeguidor + "] no existe."
                ));

        // 4. Buscar al usuario que recibe el seguimiento
        Usuario seguido = usuarioRepository.findById(idSeguido)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario con ID [" + idSeguido + "] no existe."
                ));

        // 5. Evitar seguimientos duplicados
        if (repository.existeSeguimiento(idSeguidor, idSeguido)) {
            throw new IllegalArgumentException(
                    "El usuario ya sigue a esta persona."
            );
        }

        // 6. Crear el seguimiento
        Seguidor nuevo = new Seguidor(
                seguidor,
                seguido,
                LocalDateTime.now()
        );

        // 7. Guardar el seguimiento
        return repository.save(nuevo);
    }// addSeguidor
}// class SeguidorService
