package com.ludus.api.services;
import com.ludus.api.models.*;
import com.ludus.api.repositories.*;
import com.ludus.api.security.CurrentUser;
import com.ludus.api.dtos.ReviewDtos.*;
import com.ludus.api.dtos.CatalogDtos.Page;
import com.ludus.api.dtos.CatalogDtos.Social;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Service
public class PublicacionService {
    private final PublicacionRepository reviews;
    private final UsuarioRepository users;
    private final JuegoRepository games;
    private final ComentarioRepository comments;
    private final InteraccionRepository likes;
    private final CurrentUser current;
    private final GameResolver resolver;
    private final TransactionTemplate transaction;
    public PublicacionService(PublicacionRepository reviews,UsuarioRepository users,JuegoRepository games,
        ComentarioRepository comments,InteraccionRepository likes,CurrentUser current,GameResolver resolver,
        PlatformTransactionManager manager) {
        this.reviews=reviews;this.users=users;this.games=games;this.comments=comments;this.likes=likes;
        this.current=current;this.resolver=resolver;this.transaction=new TransactionTemplate(manager);
    }
    public Review publishIgdb(Long id,Input input) {
        validate(input);
        Usuario author=current.get();
        Juego game=resolver.resolve(id);
        return save(author,game,input);
    }
    public Review publishLocal(Publicacion input) {
        if(input==null || input.getJuego()==null || input.getJuego().getId_juego()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Se requiere juego.id_juego local");
        Input body=new Input(input.getTexto_resenia(),input.getCalificacion());
        validate(body);
        Usuario author=current.get();
        Juego game=games.findById(input.getJuego().getId_juego()).orElseThrow(()->missing());
        return save(author,game,body);
    }
    private Review save(Usuario author,Juego game,Input input) {
        return transaction.execute(status-> {
            if (reviews.existsForUserAndGame(author.getId_usuario(),game.getId_juego()))
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Ya publicaste una resena para este videojuego; puedes editarla o eliminarla");
            // UNIQUE(id_usuario,id_juego) cierra la carrera entre comprobacion e INSERT.
            // ApiErrors traduce su DataIntegrityViolationException a HTTP 409 tras el rollback.
            Publicacion review=new Publicacion(null,author,game,input.calificacion(),input.texto().trim(),LocalDateTime.now());
            return dto(reviews.saveAndFlush(review));
        });
    }
    @Transactional(readOnly=true)
    public Page<Review> list(Long igdbId,Integer user,int page,int size) {
        IgdbService.pagination(page,size);
        if(igdbId!=null) IgdbService.validId(igdbId);
        if(user!=null && !users.existsById(user)) throw missing();
        var rows=reviews.list(igdbId,user,PageRequest.of(page,size,Sort.by(Sort.Order.desc("fecha_publicacion"),Sort.Order.desc("id_resenia"))));
        return new Page<>(rows.getContent().stream().map(this::dto).toList(),page,size,rows.hasNext());
    }
    @Transactional(readOnly=true) public Review detail(Integer id) {return dto(find(id));}
    @Transactional public Review update(Integer id,Input body) {
        validate(body);
        Publicacion review=find(id);
        current.owns(review.getUsuario().getId_usuario());
        review.setTexto_resenia(body.texto().trim());review.setCalificacion(body.calificacion());
        return dto(reviews.saveAndFlush(review));
    }
    @Transactional public void delete(Integer id) {
        Publicacion review=find(id);
        current.owns(review.getUsuario().getId_usuario());
        // Borrado explicito de dependientes solo tras verificar propiedad y dentro de la misma transaccion.
        comments.deleteAll(comments.findComentariosPorResenia(id));
        likes.deleteAll(likes.findByPublicacionId(id));
        reviews.delete(review);reviews.flush();
    }
    @Transactional(readOnly=true) public Social social(Long id) {
        IgdbService.validId(id);
        return new Social(reviews.average(id),reviews.countForGame(id));
    }
    private Publicacion find(Integer id) {return reviews.findById(id).orElseThrow(()->missing());}
    private ResponseStatusException missing() {return new ResponseStatusException(HttpStatus.NOT_FOUND,"Recurso no encontrado");}
    private void validate(Input input) {
        if(input==null || input.texto()==null || input.texto().isBlank() || input.texto().length()>10000
            || input.calificacion()==null || !Double.isFinite(input.calificacion())
            || input.calificacion()<1 || input.calificacion()>5)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Texto 1..10000 y calificacion de 1 a 5 requeridos");
    }
    private Review dto(Publicacion review) {
        var author=review.getUsuario();var game=review.getJuego();
        return new Review(review.getId_resenia(),review.getTexto_resenia(),review.getCalificacion(),review.getFecha_publicacion(),
            new Author(author.getId_usuario(),author.getNombre()),new com.ludus.api.dtos.ReviewDtos.Game(
                game.getId_juego(),game.getId_api(),game.getNombre_juego(),game.getImagen_url()));
    }
}
