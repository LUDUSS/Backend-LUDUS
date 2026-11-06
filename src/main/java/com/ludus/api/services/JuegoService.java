package com.ludus.api.services;
import com.ludus.api.models.Juego;
import com.ludus.api.repositories.JuegoRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class JuegoService {
    private final JuegoRepository games;
    private final GameResolver resolver;
    private final com.ludus.api.security.CurrentUser current;
    public JuegoService(JuegoRepository games,GameResolver resolver,com.ludus.api.security.CurrentUser current) {this.games=games;this.resolver=resolver;this.current=current;}
    // Compatibilidad: usar IGDB paginado para nuevo catalogo.
    public List<Juego> getJuegos() {return games.findAll();}
    public Juego getJuego(Integer id) {return games.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Juego local no encontrado"));}
    public Juego addJuego(Juego input) {current.get();return resolver.resolve(input.getId_api());}
    public Juego deleteJuego(Integer id) {throw forbidden();}
    public Juego updateJuego(Integer id,String nombre,String categoria,String plataforma,String descripcion,String imagen) {throw forbidden();}
    private ResponseStatusException forbidden() {return new ResponseStatusException(HttpStatus.FORBIDDEN,"El catalogo verificado no admite edicion ni borrado manual");}
}
