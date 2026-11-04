package com.ludus.api.repositories;

import com.ludus.api.models.Seguidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeguidorRepository extends JpaRepository<Seguidor, Integer> {

    // 1. Usuarios a los que sigue una persona
    @Query("""
        SELECT s
        FROM Seguidor s
        WHERE s.seguidor.id_usuario = :idUsuario
        """)
    List<Seguidor> findSiguiendo(
            @Param("idUsuario") Integer idUsuario
    );

    // 2. Usuarios que siguen a una persona
    @Query("""
        SELECT s
        FROM Seguidor s
        WHERE s.seguido.id_usuario = :idUsuario
        """)
    List<Seguidor> findSeguidores(
            @Param("idUsuario") Integer idUsuario
    );

    // 3. Comprobar si ya existe el seguimiento
    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Seguidor s
        WHERE s.seguidor.id_usuario = :idSeguidor
        AND s.seguido.id_usuario = :idSeguido
        """)
    boolean existeSeguimiento(
            @Param("idSeguidor") Integer idSeguidor,
            @Param("idSeguido") Integer idSeguido
    );

}// interface SeguidorRepository
