package com.ludus.api.controllers;

import com.ludus.api.dtos.LoginRequest;
import com.ludus.api.dtos.TokenJwt;
import com.ludus.api.services.JwtService;
import jakarta.servlet.ServletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/ludus/login")

public class LoginController {

    private final JwtService service;

    @Autowired
    public LoginController(JwtService service){
        this.service = service;
    }

    @PostMapping
    public TokenJwt validarUsuario(@RequestBody LoginRequest request) throws ServletException {
        if(service.validarUsuario(request)){
            return new TokenJwt(service.generarToken(request.getCorreo()));
        }
        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED,"Correo o contrasena incorrectos");
    }
}
