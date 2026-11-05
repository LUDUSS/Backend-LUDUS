package com.ludus.api.repositories;

import com.ludus.api.models.Seguidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguidorRepository extends JpaRepository<Seguidor, Integer> {

    @Query("SELECT s FROM Seguidor s WHERE s.seguidor.id_usuario = :idSeguidor")
    List<Seguidor> findBySeguidorId(@Param("idSeguidor") Integer idSeguidor);

    @Query("SELECT s FROM Seguidor s WHERE s.seguido.id_usuario = :idSeguido")
    List<Seguidor> findBySeguidoId(@Param("idSeguido") Integer idSeguido);

    @Query("SELECT COUNT(s) > 0 FROM Seguidor s WHERE s.seguidor.id_usuario = :idSeguidor AND s.seguido.id_usuario = :idSeguido")
    boolean existsBySeguidorIdAndSeguidoId(@Param("idSeguidor") Integer idSeguidor, @Param("idSeguido") Integer idSeguido);
}