package com.ludus.api.repositories;

import com.ludus.api.models.Interaccion;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InteraccionRepository extends JpaRepository<Interaccion, Integer> {

    java.util.List<Interaccion> findByIdResenia(Integer idResenia);
    boolean existsByIdUsuarioAndIdResenia(Integer idUsuario, Integer idResenia);
    java.util.Optional<Interaccion> findByIdUsuarioAndIdResenia(Integer idUsuario, Integer idResenia);}// interface InteraccionesRepository
