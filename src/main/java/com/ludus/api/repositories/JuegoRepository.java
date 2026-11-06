package com.ludus.api.repositories;

import com.ludus.api.models.Juego;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JuegoRepository extends JpaRepository<Juego,Integer> {
    @org.springframework.data.jpa.repository.Query("select j from Juego j where j.id_api = :id")
    java.util.Optional<Juego> findByIgdbId(@org.springframework.data.repository.query.Param("id") Long id);
}
