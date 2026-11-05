package com.ludus.api.repositories;

import com.ludus.api.models.Interaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteraccionRepository extends JpaRepository<Interaccion, Integer> {

    @Query("SELECT i FROM Interaccion i WHERE i.publicacion.id_resenia = :idResenia")
    List<Interaccion> findByPublicacionId(@Param("idResenia") Integer idResenia);

    @Query("SELECT i FROM Interaccion i WHERE i.usuario.id_usuario = :idUsuario")
    List<Interaccion> findByUsuarioId(@Param("idUsuario") Integer idUsuario);
}