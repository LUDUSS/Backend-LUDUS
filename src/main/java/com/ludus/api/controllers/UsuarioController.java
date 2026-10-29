package com.ludus.api.controllers;

import com.ludus.api.models.Usuario;
import com.ludus.api.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping (path = "/ludus/usuario/")

public class UsuarioController {
    private final UsuarioService service;
    @Autowired// -> Gestiona la inyección de dependencias de forma automatica
    // -> INICIO CONSTRUCTOR PARA  UsuarioController
    public UsuarioController(UsuarioService service){
        this.service = service;
    }// fin constructor

    @GetMapping
    public List<Usuario> getUsuario(){
        return  service.getUsuario();
    }// fin LIST getUsuario

    // SEGUNDO METODO GET HTTP
    @GetMapping("{id_usuario}")
    // Método Público
    public Usuario getUsuario(@PathVariable Integer id_usuario){
        return service.getUsuario(id_usuario);
    }// fin getUsuario

    // borrar un usuario con metodo delete
    @DeleteMapping("{id_usuario}")
    public  Usuario deleteUsuario(@PathVariable Integer id_usuario){
        return service.deleteUsuario(id_usuario);
    }// fin deleteUsuario

    // agregar un usuario
    @PostMapping
    public  Usuario addUsuario(@RequestBody Usuario usuario){ // -> Solicita los datos del usuario en el cuerpo de la solicitud en formato JSON
        return  service.addUsuario(usuario);
    }// fin addUsuario

    // editar un usuario
    @PutMapping("/{id_usuario}")
    public Usuario updateUsuario(@PathVariable Integer id_usuario,
                                 @RequestBody Usuario usuario) {

        return service.updateUsuario(
                id_usuario,
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getContrasenia(),
                usuario.getTelefono(),
                usuario.getSobre_mi()
        );
    }
}// fin class UsuarioController;
