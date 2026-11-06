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
    }

    public List<Seguidor> getSeguidores() {
        return repository.findAll();
    }

    public List<Seguidor> getSiguiendo(Integer idUsuario) {
        return repository.findBySeguidorId(idUsuario);
    }

    public List<Seguidor> getSeguidoresDe(Integer idUsuario) {
        return repository.findBySeguidoId(idUsuario);
    }

    public Seguidor addSeguidor(Integer idSeguidor, Integer idSeguido) {
        if (idSeguidor.equals(idSeguido)) {
            throw new IllegalArgumentException("Un usuario no puede seguirse a sí mismo");
        }

        if (repository.existsBySeguidorIdAndSeguidoId(idSeguidor, idSeguido)) {
            throw new IllegalArgumentException("El usuario " + idSeguidor + " ya sigue al usuario " + idSeguido);
        }

        Usuario seguidor = usuarioRepository.findById(idSeguidor)
                .orElseThrow(() -> new IllegalArgumentException("El usuario seguidor con ID " + idSeguidor + " no existe."));
        Usuario seguido = usuarioRepository.findById(idSeguido)
                .orElseThrow(() -> new IllegalArgumentException("El usuario a seguir con ID " + idSeguido + " no existe."));

        Seguidor nuevo = new Seguidor();
        nuevo.setSeguidor(seguidor);
        nuevo.setSeguido(seguido);
        nuevo.setFechaSeguimiento(LocalDateTime.now());

        return repository.save(nuevo);
    }

    public Seguidor deleteSeguidor(Integer idSeguimiento) {
        Seguidor seguidor = null;
        if (repository.existsById(idSeguimiento)) {
            seguidor = repository.findById(idSeguimiento).get();
            repository.delete(seguidor);
        }
        return seguidor;
    }

    // Dejar de seguir sin conocer el id del seguimiento (el frontend lo necesita)
    public Seguidor deleteSeguidor(Integer idSeguidor, Integer idSeguido) {
        return repository.findBySeguidorIdAndSeguidoId(idSeguidor, idSeguido)
                .map(s -> {
                    repository.delete(s);
                    return s;
                })
                .orElse(null);
    }
}
