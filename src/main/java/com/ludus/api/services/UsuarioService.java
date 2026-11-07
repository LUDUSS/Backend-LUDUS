package com.ludus.api.services;

import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder; // <--- Inyectamos el encriptador

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder){
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> getUsuario(){
        return repository.findAll();
    }

    public Usuario getUsuario(Integer id_usuario) {
        return repository.findById(id_usuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario con el ID ["+ id_usuario +"] no existe"));
    }

    public Usuario getUsuarioPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return null;
        }
        return repository.findByCorreo(correo.trim().toLowerCase()).orElse(null);
    }

    public List<Usuario> buscarPorNombre(String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        return repository.findByNombreContainingIgnoreCase(q.trim());
    }

    public Usuario updatePrivacidad(Integer idUsuario, boolean perfilPublico) {
        if (repository.existsById(idUsuario)) {
            Usuario usuario = repository.findById(idUsuario).get();
            usuario.setPerfil_publico(perfilPublico);
            return repository.save(usuario);
        }
        return null;
    }

    public Usuario updateJuegoActivo(Integer idUsuario, String juegoActivo) {
        if (repository.existsById(idUsuario)) {
            Usuario usuario = repository.findById(idUsuario).get();
            usuario.setJuego_activo(juegoActivo != null ? juegoActivo.trim() : null);
            return repository.save(usuario);
        }
        return null;
    }

    public Usuario deleteUsuario(Integer id_usuario) {
        Usuario usuario = null;
        if(repository.existsById(id_usuario)){
            usuario = repository.findById(id_usuario).get();
            repository.delete(usuario);
        }
        return usuario;
    }

    public Usuario addUsuario(Usuario usuario){
        if(usuario.getNombre() != null && usuario.getCorreo() != null && usuario.getContrasenia() != null && usuario.getTelefono() != null
                && !usuario.getNombre().isBlank() && !usuario.getCorreo().isBlank() && !usuario.getContrasenia().isBlank() && !usuario.getTelefono().isBlank()){

            // Encriptamos usando el bean inyectado
            usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));

            if(usuario.getFecha_registro() == null) {
                usuario.setFecha_registro(LocalDateTime.now());
            }

            if(usuario.getSobre_mi() == null) {
                usuario.setSobre_mi("");
            }

            return repository.save(usuario);
        }
        throw new IllegalArgumentException("Faltan campos obligatorios del usuario");
    }

    public Usuario updateUsuario(Integer idUsuario, String nombre, String correo, String contrasenia, String telefono, String sobre_mi) {
        Usuario usuario = null;
        if(repository.existsById(idUsuario)){
            Usuario us = repository.findById(idUsuario).get();
            if (nombre != null && !nombre.isBlank()) {
                us.setNombre(nombre);
            }
            if (correo != null && !correo.isBlank()) {
                us.setCorreo(correo);
            }
            if (contrasenia != null && !contrasenia.isBlank()) {
                us.setContrasenia(passwordEncoder.encode(contrasenia)); // Encriptamos aquí también
            }
            if (telefono != null && !telefono.isBlank()) {
                us.setTelefono(telefono);
            }
            if (sobre_mi != null) {
                us.setSobre_mi(sobre_mi);
            }
            usuario = us;
            repository.save(usuario);
        }
        return usuario;
    }
}
