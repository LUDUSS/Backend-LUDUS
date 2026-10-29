package com.ludus.api.services;

import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Service
public class UsuarioService {
    // conectar UsuarioRepository
    private final UsuarioRepository repository;
    // CONSTRUCTOR DE UsuarioService
    public UsuarioService(UsuarioRepository repository){
        this.repository = repository;
    }// fin constructor

    public List<Usuario> getUsuario(){
        return  repository.findAll(); // -> QUERY (SELECT * FROM Usuario)
    }

    public Usuario getUsuario(Integer id_usuario) {
        return repository
                .findById(id_usuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario con el ID ["+ id_usuario +"] no existe"));
    }// fin getUsuario(id)

    public Usuario deleteUsuario (Integer id_usuario){
        Usuario usuario = null;
        if(repository.existsById(id_usuario)){// buscar el usuario por id
            usuario = repository.findById(id_usuario).get();
            repository.delete(usuario);
        }// fin if
        return usuario;
    }// fin deleteUsuario

    // Crear un nuevo usuario
    public Usuario addUsuario(Usuario usuario){
        // validacion
        if(!usuario.getNombre().isBlank() && !usuario.getCorreo().isBlank() && !usuario.getContrasenia().isBlank() && !usuario.getTelefono().isBlank()){
            repository.save(usuario);
        }// fin if
        return usuario;
    }// fin addUsuario

    public Usuario updateUsuario(Integer idUsuario, String nombre, String correo, String contrasenia, String telefono, String sobre_mi) {
        Usuario usuario = null;
        // encontrar el usuario con el id
        if(repository.existsById(idUsuario)){
            Usuario us = repository.findById(idUsuario).get();
            // Actualizar campos solo si se proporcionan nuevos valores válidos
            if (nombre != null && !nombre.isBlank()) {
                us.setNombre(nombre);
            }
            if (correo != null && !correo.isBlank()) {
                us.setCorreo(correo);
            }
            if (contrasenia != null && !contrasenia.isBlank()) {
                us.setContrasenia(contrasenia);
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
    } // fin updateUsuario
}// fin class UsuarioService
