package com.ludus.api.services;

import com.ludus.api.models.Juego;
import com.ludus.api.repositories.JuegoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JuegoService {

    private final JuegoRepository repository;

    public JuegoService(JuegoRepository repository) {
        this.repository = repository;
    }// constructor

    public List<Juego> getJuegos(){
        return repository.findAll();
    }// getJuegos

    public Juego getJuego(Integer id_juego){
        return repository
                .findById(id_juego)
                .orElseThrow( () -> new IllegalArgumentException("El usuario con el ID ["+ id_juego +"] no existe") );
    }// getJuego

    public Juego deleteJuego(Integer id_juego){
        Juego juego = null;

        if(repository.existsById(id_juego)){
            juego = repository.findById(id_juego).get();
            repository.delete(juego);
        }
        return juego;
    }// deleteJuego

    public Juego addJuego(Juego juego){
        if(!juego.getNombre_juego().isBlank() && !juego.getCategoria().isBlank() && !juego.getPlataforma().isBlank() && !juego.getDescripcion().isBlank() && !juego.getImagen_url().isBlank()){
            repository.save(juego);
        }
        return juego;
    }// addJuego


    public Juego updateJuego(Integer idJuego, String nombreJuego, String categoria, String plataforma, String descripcion, String imagenUrl) {
        Juego juego = null;

        if(repository.existsById(idJuego)){
            Juego jg = repository.findById(idJuego).get();
            if(nombreJuego != null && !nombreJuego.isBlank() ){
                jg.setNombre_juego(nombreJuego);
            }
            if(categoria != null && !categoria.isBlank() ){
                jg.setCategoria(categoria);
            }
            if(plataforma != null && !plataforma.isBlank() ){
                jg.setPlataforma(plataforma);
            }
            if(descripcion != null && !descripcion.isBlank() ){
                jg.setDescripcion(descripcion);
            }
            if(imagenUrl != null && !imagenUrl.isBlank() ){
                jg.setImagen_url(imagenUrl);
            }
            juego = jg;
            repository.save(juego);
        }
        return juego;
    }// updateJuego
}
