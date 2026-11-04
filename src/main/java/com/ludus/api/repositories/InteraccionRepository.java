package com.ludus.api.repositories;

import com.ludus.api.models.Interaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface InteraccionRepository extends JpaRepository<Interaccion, Integer> {

    @Query("""
    SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
    FROM Interaccion i
    WHERE i.usuario.id_usuario = :idUsuario
    AND i.publicacion.id_resenia = :idResenia
    """)
    boolean existeLike(
            @Param("idUsuario") Integer idUsuario,
            @Param("idResenia") Integer idResenia
    );

}// interface InteraccionesRepository
