package com.ludus.api.controllers;

import com.ludus.api.models.Usuario;
import com.ludus.api.security.CurrentUser;
import com.ludus.api.services.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/ludus/usuario")
public class UsuarioController {
    
    private final CurrentUser currentUser;
    private final UsuarioService service;

    public UsuarioController(CurrentUser currentUser, UsuarioService service) {
        this.currentUser = currentUser;
        this.service = service;
    }

    @GetMapping
    public List<Usuario> getUsuario() {
        return service.getUsuario();
    }

    @GetMapping("{id_usuario}")
    public Usuario getUsuario(@PathVariable Integer id_usuario) {
        return service.getUsuario(id_usuario);
    }

    @GetMapping("correo/{correo}")
    public Usuario getUsuarioPorCorreo(@PathVariable String correo) {
        return service.getUsuarioPorCorreo(correo);
    }

    @DeleteMapping("{id_usuario}")
    public Usuario deleteUsuario(@PathVariable Integer id_usuario) {
        currentUser.owns(id_usuario);
        return service.deleteUsuario(id_usuario);
    }

    @PostMapping
    public Usuario addUsuario(@RequestBody Usuario usuario) {
        return service.addUsuario(usuario);
    }

    @PutMapping("{id_usuario}")
    public Usuario updateUsuario(@PathVariable Integer id_usuario,
                                 @RequestBody Usuario usuario) {
        currentUser.owns(id_usuario);

        return service.updateUsuario(
                id_usuario,
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getContrasenia(),
                usuario.getTelefono(),
                usuario.getSobre_mi()
        );
    }
}
