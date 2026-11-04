package com.ludus.api.repositories;

import com.ludus.api.models.Juego;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JuegoRepository extends JpaRepository<Juego,Integer> {
}
