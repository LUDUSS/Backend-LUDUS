package com.ludus.api.services;

import com.ludus.api.models.Interaccion;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.InteraccionRepository;
import com.ludus.api.repositories.PublicacionRepository;
import com.ludus.api.security.CurrentUser;
import com.ludus.api.dtos.LikeState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class InteraccionesService {
    private final InteraccionRepository repository;
    private final PublicacionRepository reviews;
    private final CurrentUser current;

    public InteraccionesService(InteraccionRepository repository, PublicacionRepository reviews, CurrentUser current) {
        this.repository=repository; this.reviews=reviews; this.current=current;
    }

    @Transactional(readOnly=true)
    public List<Interaccion> getInteracciones() { return repository.findAll(); }

    @Transactional(readOnly=true)
    public Interaccion getInteraccion(Integer id) {
        positive(id);
        return repository.findById(id).orElseThrow(()->missing("Like no encontrado"));
    }

    @Transactional
    public Interaccion addInteraccion(Interaccion input) {
        if(input==null || input.getPublicacion()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"publicacion.id_resenia es obligatorio");
        // El contrato heredado no puede elegir usuario, id ni fecha.
        return create(input.getPublicacion().getId_resenia(),current.get());
    }

    @Transactional
    public LikeState like(Integer idResenia) {
        Usuario user=current.get();
        create(idResenia,user);
        return summaries(List.of(idResenia),user.getId_usuario()).getFirst();
    }

    private Interaccion create(Integer idResenia,Usuario user) {
        positive(idResenia);
        var review=reviews.findById(idResenia).orElseThrow(()->missing("Resena no encontrada"));
        if(repository.existsForUserAndReview(user.getId_usuario(),idResenia))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Ya diste like a esta resena");
        // Se conserva la posibilidad existente de dar like a una resena propia.
        var interaction=new Interaccion();
        interaction.setUsuario(user);interaction.setPublicacion(review);interaction.setFecha_like(LocalDateTime.now());
        // UNIQUE cierra la carrera; ApiErrors devuelve 409 si otra solicitud gano.
        return repository.saveAndFlush(interaction);
    }

    @Transactional
    public void unlike(Integer idResenia) {
        positive(idResenia);
        Integer user=current.get().getId_usuario();
        if(!reviews.existsById(idResenia)) throw missing("Resena no encontrada");
        // Idempotente para una resena existente; solo afecta la pareja del JWT.
        repository.deleteForUserAndReview(user,idResenia);
    }

    @Transactional
    public void deleteInteraccion(Integer id) {
        var interaction=getInteraccion(id);
        current.owns(interaction.getUsuario().getId_usuario());
        repository.delete(interaction);
        repository.flush();
    }

    @Transactional(readOnly=true)
    public LikeState state(Integer idResenia) {
        positive(idResenia);
        return summaries(List.of(idResenia),current.get().getId_usuario()).getFirst();
    }

    @Transactional(readOnly=true)
    public List<LikeState> states(List<Integer> ids) {
        if(ids==null || ids.isEmpty() || ids.size()>50)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Se requieren entre 1 y 50 ids de resena");
        ids.forEach(this::positive);
        return summaries(new ArrayList<>(new LinkedHashSet<>(ids)),current.get().getId_usuario());
    }

    private List<LikeState> summaries(List<Integer> ids,Integer user) {
        var indexed=new HashMap<Integer,LikeState>();
        for(var row:repository.summarize(ids,user))
            indexed.put(row.getIdResenia(),new LikeState(row.getIdResenia(),row.getTotalLikes(),
                row.getMine()!=null && row.getMine()>0,row.getIdInteraccion()));
        if(indexed.size()!=ids.size()) throw missing("Resena no encontrada");
        return ids.stream().map(indexed::get).toList();
    }

    private void positive(Integer id) {
        if(id==null || id<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID de resena o interaccion positivo requerido");
    }
    private ResponseStatusException missing(String reason) {return new ResponseStatusException(HttpStatus.NOT_FOUND,reason);}
}
