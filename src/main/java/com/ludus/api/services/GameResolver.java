package com.ludus.api.services;
import com.ludus.api.models.Juego;
import com.ludus.api.repositories.JuegoRepository;
import com.ludus.api.dtos.CatalogDtos.Game;
import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DataIntegrityViolationException;
@Service
public class GameResolver {
    private final JuegoRepository games;
    private final IgdbService igdb;
    private final TransactionTemplate insert;
    public GameResolver(JuegoRepository games,IgdbService igdb,PlatformTransactionManager manager) {
        this.games=games;this.igdb=igdb;
        insert=new TransactionTemplate(manager);
        insert.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }
    public Juego resolve(Long id) {
        IgdbService.validId(id);
        var existing=games.findByIgdbId(id);
        if(existing.isPresent()) return existing.get();
        Game external=igdb.detalle(id);
        try {
            return insert.execute(status-> {
                var winner=games.findByIgdbId(id);
                if(winner.isPresent()) return winner.get();
                var game=new Juego();
                game.setId_api(id);game.setNombre_juego(external.nombre());
                game.setDescripcion(external.descripcion());game.setImagen_url(external.portadaUrl());
                // Representacion temporal compatible: nombres separados por coma; DTO externo conserva listas e IDs.
                game.setCategoria(external.generos().stream().map(x->x.nombre()).collect(java.util.stream.Collectors.joining(", ")));
                game.setPlataforma(external.plataformas().stream().map(x->x.nombre()).collect(java.util.stream.Collectors.joining(", ")));
                return games.saveAndFlush(game);
            });
        } catch(DataIntegrityViolationException race) {
            // La transaccion fallida ya termino antes de leer al ganador.
            return games.findByIgdbId(id).orElseThrow(()->race);
        }
    }
}
