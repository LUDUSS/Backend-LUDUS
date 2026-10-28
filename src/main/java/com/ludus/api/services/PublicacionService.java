package com.ludus.api.services;

import com.ludus.api.models.Publicacion;
import com.ludus.api.repositories.PublicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Service
public class PublicacionService {
    // conectar PublicacionRepository
    private final PublicacionRepository repository;
    // CONSTRUCTOR DE PublicacionService
    public PublicacionService(PublicacionRepository repository){
        this.repository = repository;
    }// fin constructor

    public List<Publicacion> getPublicacion(){
        return  repository.findAll(); // -> QUERY (SELECT * FROM Publicacion)
    }

    public Publicacion getPublicacion(Integer id_resenia) {
        return repository
                .findById(id_resenia)
                .orElseThrow(() -> new IllegalArgumentException("La publicacion con el ID ["+ id_resenia +"] no existe"));
    }// fin getPublicacion (id)

    public Publicacion deletePublicacion (Integer id_resenia){
        Publicacion publicacion = null;
        if(repository.existsById(id_resenia)){// buscar la publicacaion or id
            publicacion = repository.findById(id_resenia).get();
            repository.delete(publicacion);
        }// fin if
        return publicacion;
    }// fin deletePublicacion

    // Crear una nueva resenia
    public Publicacion addPublicacion(Publicacion publicacion){
        // validacion
        if(!publicacion.getTexto_resenia().isBlank() && !publicacion.getCalificacion().isNaN()){;
        repository.save(publicacion);
        }// fin if
        return publicacion;
    }// fin addPublicacion

    public Publicacion updatePublicacion(Integer idResenia, Double calificacion, String textoResenia) {
        Publicacion publicacion = null;
        // encontrar la publiccaion con el id
        if(repository.existsById(idResenia)){
            Publicacion p = repository.findById(idResenia).get();
            if(calificacion != null && !calificacion.isNaN()) p.setCalificacion((calificacion));
            publicacion = p;
            repository.save(publicacion);
        }
        return publicacion;
    }
}// fin class PublicacionService
