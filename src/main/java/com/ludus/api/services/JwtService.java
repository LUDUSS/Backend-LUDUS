package com.ludus.api.services;

import com.ludus.api.dtos.LoginRequest;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.UsuarioRepository;
import com.ludus.api.security.JwtFilter;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    @Autowired
    private PasswordEncoder encoder;

    private final UsuarioRepository usuarioRepository;

    public JwtService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }

    public boolean validarUsuario(LoginRequest request) {
        Optional<Usuario> usuario = usuarioRepository.findByCorreo(request.getCorreo());
        if(usuario.isPresent()){
            Usuario usr = usuario.get();
            // Comparamos la contraseña enviada en texto plano con el hash encriptado de la BD
            if(encoder.matches(request.getContrasenia(), usr.getContrasenia())){
                return true;
            }
        }
        return false;
    }

    public String generarToken(String correo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 3); // Expira en 3 horas

        return Jwts
                .builder()
                .subject(correo)
                .issuedAt(new Date())
                .expiration(calendar.getTime())
                .signWith(JwtFilter.getSignInKey())
                .compact();
    }
}