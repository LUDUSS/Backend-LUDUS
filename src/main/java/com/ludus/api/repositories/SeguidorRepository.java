package com.ludus.api.repositories;

import com.ludus.api.models.Seguidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguidorRepository extends JpaRepository<Seguidor, Integer> {

    // A quiénes sigue un usuario (Siguiendo) — usa Integer temporal
    List<Seguidor> findById_seguidor(Integer idSeguidor);

    // Quiénes siguen a un usuario (Seguidores) — usa Integer temporal
    List<Seguidor> findById_seguido(Integer idSeguido);

    // Para evitar duplicados
    boolean existsById_seguidorAndId_seguido(Integer idSeguidor, Integer idSeguido);
}// interface SeguidorRepository
