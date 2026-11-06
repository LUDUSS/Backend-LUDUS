package com.ludus.api.repositories;

import com.ludus.api.models.Interaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteraccionRepository extends JpaRepository<Interaccion, Integer> {
    @Query("select count(i) > 0 from Interaccion i where i.usuario.id_usuario=:user and i.publicacion.id_resenia=:review")
    boolean existsForUserAndReview(@Param("user") Integer user, @Param("review") Integer review);

    @org.springframework.data.jpa.repository.Modifying
    @Query("delete from Interaccion i where i.usuario.id_usuario=:user and i.publicacion.id_resenia=:review")
    int deleteForUserAndReview(@Param("user") Integer user, @Param("review") Integer review);

    interface LikeSummary {
        Integer getIdResenia();
        Long getTotalLikes();
        Long getMine();
        Integer getIdInteraccion();
    }

    // Una sola consulta agregada para toda la pagina, incluyendo resenas sin likes.
    @Query("select p.id_resenia as idResenia, count(i) as totalLikes, " +
        "sum(case when i.usuario.id_usuario=:user then 1 else 0 end) as mine, " +
        "max(case when i.usuario.id_usuario=:user then i.id_interaccion else null end) as idInteraccion " +
        "from Publicacion p left join Interaccion i on i.publicacion=p " +
        "where p.id_resenia in :ids group by p.id_resenia")
    List<LikeSummary> summarize(@Param("ids") java.util.Collection<Integer> ids, @Param("user") Integer user);

    @Query("SELECT i FROM Interaccion i WHERE i.publicacion.id_resenia = :idResenia")
    List<Interaccion> findByPublicacionId(@Param("idResenia") Integer idResenia);

    @Query("SELECT i FROM Interaccion i WHERE i.usuario.id_usuario = :idUsuario")
    List<Interaccion> findByUsuarioId(@Param("idUsuario") Integer idUsuario);
}
