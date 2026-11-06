package com.ludus.api.services;
import com.ludus.api.igdb.*;
import com.ludus.api.dtos.CatalogDtos.*;
import com.ludus.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class IgdbService {
    private static final String FIELDS="fields id,name,summary,genres.id,genres.name,platforms.id,platforms.name,cover.image_id,first_release_date,rating,slug,screenshots.image_id;";
    private final IgdbClient client;
    private final JuegoRepository games;
    private final PublicacionRepository reviews;
    public IgdbService(IgdbClient client,JuegoRepository games,PublicacionRepository reviews) {
        this.client=client;this.games=games;this.reviews=reviews;
    }
    public Page<Game> buscarJuegos(String nombre,int page,int size,String sort,Long genre,Long platform) {
        if(nombre==null || nombre.isBlank() || nombre.length()>100 || nombre.codePoints().anyMatch(Character::isISOControl))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Nombre requerido, hasta 100 caracteres sin controles");
        if(!sort.equals("relevancia")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Buscar solo admite sort=relevancia");
        return query(nombre,page,size,sort,genre,platform);
    }
    public Page<Game> explorar(int page,int size,String sort,Long genre,Long platform) {return query(null,page,size,sort,genre,platform);}
    private Page<Game> query(String name,int page,int size,String sort,Long genre,Long platform) {
        pagination(page,size);
        var where=new ArrayList<String>();
        if(genre!=null) {validId(genre);where.add("genres = ("+genre+")");}
        if(platform!=null) {validId(platform);where.add("platforms = ("+platform+")");}
        String sorting="";
        if(name==null) {
            sorting=switch(sort) {
                case "rating" -> {where.add("rating != null");yield "sort rating desc;";}
                case "lanzamiento" -> {where.add("first_release_date != null");yield "sort first_release_date desc;";}
                case "nombre" -> "sort name asc;";
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"sort debe ser rating, lanzamiento o nombre");
            };
        }
        String search=name==null?"":"search \""+name.trim().replace("\\","\\\\").replace("\"","\\\"")+"\";";
        String filter=where.isEmpty()?"":"where "+String.join(" & ",where)+";";
        IgdbGame[] raw=client.games(search+FIELDS+filter+sorting+"limit "+(size+1)+";offset "+(page*size)+";");
        List<Game> items=Arrays.stream(raw).limit(size).map(this::map).toList();
        return new Page<>(items,page,size,raw.length>size);
    }
    public Game detalle(Long id) {
        validId(id);
        var result=client.games(FIELDS+"where id = "+id+";limit 1;");
        if(result.length==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Juego IGDB no encontrado");
        if(result[0]==null || !id.equals(result[0].id())) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"ID de juego IGDB inconsistente");
        return map(result[0]);
    }
    public Page<Lookup> lookup(String endpoint,int page,int size) {
        pagination(page,size);
        var rows=client.lookup(endpoint,"fields id,name;sort name asc;limit "+(size+1)+";offset "+(page*size)+";");
        return new Page<>(Arrays.stream(rows).limit(size).map(x->new Lookup(x.id(),x.name())).toList(),page,size,rows.length>size);
    }
    public Game map(IgdbGame game) {
        if(game==null || game.id()==null || game.name()==null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Juego IGDB incompleto");
        var local=games.findByIgdbId(game.id()).orElse(null);
        return new Game(game.id(),local==null?null:local.getId_juego(),game.name(),game.summary(),
            image(game.cover(),"cover_big"),names(game.genres()),names(game.platforms()),game.first_release_date(),
            game.rating(),game.slug(),game.screenshots()==null?List.of():game.screenshots().stream()
                .map(x->image(x,"screenshot_big")).filter(Objects::nonNull).toList(),
            new Social(reviews.average(game.id()),reviews.countForGame(game.id())));
    }
    private List<Lookup> names(List<IgdbGame.Named> list) {
        return list==null?List.of():list.stream().filter(Objects::nonNull).map(x->new Lookup(x.id(),x.name())).toList();
    }
    public static String image(IgdbGame.Image image,String size) {
        if(image==null || image.image_id()==null || !image.image_id().matches("[A-Za-z0-9_]+")) return null;
        return "https://images.igdb.com/igdb/image/upload/t_"+size+"/"+image.image_id()+".jpg";
    }
    public static void pagination(int page,int size) {
        if(page<0 || page>1000 || size<1 || size>50) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"page 0..1000; size 1..50");
    }
    public static void validId(Long id) {
        if(id==null || id<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID positivo requerido");
    }
}
