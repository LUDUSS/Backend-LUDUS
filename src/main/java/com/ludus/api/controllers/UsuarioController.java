package com.ludus.api.controllers;

import com.ludus.api.models.Usuario;
import com.ludus.api.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/ludus/usuario/")
@CrossOrigin(origins = "*")
public class UsuarioController {
    private final UsuarioService service;

    @Autowired
    public UsuarioController(UsuarioService service) {
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
        return service.deleteUsuario(id_usuario);
    }

    @PostMapping
    public Usuario addUsuario(@RequestBody Usuario usuario) {
        return service.addUsuario(usuario);
    }

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

    @PatchMapping("/{id_usuario}/privacidad")
    public Usuario updatePrivacidad(@PathVariable Integer id_usuario,
                                    @RequestParam boolean perfil_publico) {
        return service.updatePrivacidad(id_usuario, perfil_publico);
    }

    @PatchMapping("/{id_usuario}/juego-activo")
    public Usuario updateJuegoActivo(@PathVariable Integer id_usuario,
                                     @RequestParam(required = false) String juego_activo) {
        return service.updateJuegoActivo(id_usuario, juego_activo);
    }

    @GetMapping("buscar")
    public List<Usuario> buscarUsuarios(@RequestParam String q) {
        return service.buscarPorNombre(q);
    }
}
