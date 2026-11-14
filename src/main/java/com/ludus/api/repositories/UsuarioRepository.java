package com.ludus.api.repositories;

import com.ludus.api.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByGamertag(String gamertag);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR (u.gamertag IS NOT NULL AND LOWER(u.gamertag) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Usuario> buscarPorNombreOGamertag(@Param("q") String q);
}
