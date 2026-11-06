package com.ludus.api.repositories;
import com.ludus.api.models.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicacionRepository  extends JpaRepository <Publicacion, Integer> {
    @org.springframework.data.jpa.repository.Query("select count(p) > 0 from Publicacion p where p.usuario.id_usuario=:user and p.juego.id_juego=:game")
    boolean existsForUserAndGame(@org.springframework.data.repository.query.Param("user") Integer user,
        @org.springframework.data.repository.query.Param("game") Integer game);
    @org.springframework.data.jpa.repository.Query("select p from Publicacion p where (:game is null or p.juego.id_api=:game) and (:user is null or p.usuario.id_usuario=:user)")
    org.springframework.data.domain.Slice<Publicacion> list(@org.springframework.data.repository.query.Param("game") Long game,
        @org.springframework.data.repository.query.Param("user") Integer user, org.springframework.data.domain.Pageable page);
    @org.springframework.data.jpa.repository.Query("select avg(p.calificacion) from Publicacion p where p.juego.id_api=:id")
    Double average(@org.springframework.data.repository.query.Param("id") Long id);
    @org.springframework.data.jpa.repository.Query("select count(p) from Publicacion p where p.juego.id_api=:id")
    long countForGame(@org.springframework.data.repository.query.Param("id") Long id);
    @org.springframework.data.jpa.repository.Query("select count(p) from Publicacion p where p.usuario.id_usuario=:id")
    long countForUser(@org.springframework.data.repository.query.Param("id") Integer id);
    @org.springframework.data.jpa.repository.Query("select count(distinct p.juego.id_juego) from Publicacion p where p.usuario.id_usuario=:id")
    long gamesForUser(@org.springframework.data.repository.query.Param("id") Integer id);
    @org.springframework.data.jpa.repository.Query("select avg(p.calificacion) from Publicacion p where p.usuario.id_usuario=:id")
    Double averageForUser(@org.springframework.data.repository.query.Param("id") Integer id);
    @org.springframework.data.jpa.repository.Query("select count(i) from Interaccion i where i.publicacion.usuario.id_usuario=:id")
    long likesForAuthor(@org.springframework.data.repository.query.Param("id") Integer id);
}// fin ProductoRepository
