package com.ludus.api.repositories;

import com.ludus.api.models.Seguidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguidorRepository extends JpaRepository<Seguidor, Integer> {

    // A quiénes sigue un usuario (Siguiendo) — usa Integer temporal
    List<Seguidor> findByIdSeguidor(Integer idSeguidor);

    // Quiénes siguen a un usuario (Seguidores) — usa Integer temporal
    List<Seguidor> findByIdSeguido(Integer idSeguido);

    // Para evitar duplicados
    boolean existsByIdSeguidorAndIdSeguido(Integer idSeguidor, Integer idSeguido);
}// interface SeguidorRepository
