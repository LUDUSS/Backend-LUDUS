package com.ludus.api.controllers;
import com.ludus.api.dtos.ReviewDtos.Profile;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.UsuarioRepository;
import com.ludus.api.security.CurrentUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/ludus/perfil")
public class PerfilController {
    private final CurrentUser current;
    private final UsuarioRepository users;
    private final com.ludus.api.repositories.PublicacionRepository reviews;
    public PerfilController(CurrentUser current,UsuarioRepository users,com.ludus.api.repositories.PublicacionRepository reviews) {this.current=current;this.users=users;this.reviews=reviews;}
    public record Edit(String nombre,String sobreMi) {}
    private Profile dto(Usuario user) {
        Integer id=user.getId_usuario();
        return new Profile(id,user.getNombre(),user.getSobre_mi(),reviews.countForUser(id),reviews.gamesForUser(id),
            reviews.averageForUser(id),reviews.likesForAuthor(id));
    }
    @GetMapping("/me") public Profile me() {return dto(current.get());}
    @GetMapping("/{id}") public Profile profile(@PathVariable Integer id) {
        return dto(users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Perfil no encontrado")));
    }
    @PutMapping("/me") @Transactional public Profile update(@RequestBody Edit input) {
        if(input.nombre()==null || input.nombre().isBlank() || input.nombre().length()>100 ||
            input.sobreMi()==null || input.sobreMi().length()>240)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Nombre 1..100 y sobreMi 0..240 requeridos");
        var user=current.get();user.setNombre(input.nombre().trim());user.setSobre_mi(input.sobreMi());
        return dto(users.save(user));
    }
}
