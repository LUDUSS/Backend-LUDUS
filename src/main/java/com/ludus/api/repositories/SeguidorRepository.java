package com.ludus.api.repositories;

import com.ludus.api.models.Seguidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguidorRepository extends JpaRepository<Seguidor, Integer> {

    // A quiÃ©nes sigue un usuario (Siguiendo) â€” usa Integer temporal
    List<Seguidor> findByIdSeguidor(Integer idSeguidor);

    // QuiÃ©nes siguen a un usuario (Seguidores) â€” usa Integer temporal
    List<Seguidor> findByIdSeguido(Integer idSeguido);

    // Para evitar duplicados
    boolean existsByIdSeguidorAndIdSeguido(Integer idSeguidor, Integer idSeguido);
    // Buscar registro de seguimiento por par (para dejar de seguir sin conocer el id)
    java.util.Optional<Seguidor> findByIdSeguidorAndIdSeguido(Integer idSeguidor, Integer idSeguido);
}// interface SeguidorRepository
