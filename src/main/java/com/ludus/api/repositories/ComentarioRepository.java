package com.ludus.api.repositories;

import com.ludus.api.models.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    @Query("SELECT c FROM Comentario c WHERE c.id_resenia = :idResenia ORDER BY c.fecha_comentario ASC")
    List<Comentario> findComentariosPorResenia(@Param("idResenia") Integer idResenia);
}
