package com.ludus.api.repositories;
import com.ludus.api.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UsuarioRepository  extends JpaRepository<Usuario, Integer> {
}
