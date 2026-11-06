package com.ludus.api.security;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Component
public class CurrentUser {
    private final UsuarioRepository users;
    private final HttpServletRequest request;
    public CurrentUser(UsuarioRepository users,HttpServletRequest request) {this.users=users;this.request=request;}
    public Usuario get() {
        Object correo=request.getAttribute("ludus.correo");
        if(!(correo instanceof String)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"JWT LUDUS requerido");
        return users.findByCorreo((String)correo).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Usuario del JWT no disponible"));
    }
    public void owns(Integer id) {
        if(!get().getId_usuario().equals(id)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Operacion reservada al autor");
    }
}
