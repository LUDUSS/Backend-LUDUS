package com.ludus.api.controllers;

import com.ludus.api.models.Interaccion;
import com.ludus.api.services.InteraccionesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ludus/interaccion/")
@CrossOrigin(origins = "*")
public class InteraccionController {

    private final InteraccionesService service;

    public InteraccionController(InteraccionesService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Interaccion>> getInteracciones() {
        return ResponseEntity.ok(service.getInteracciones());
    }

    @GetMapping("/{id_interaccion}")
    public ResponseEntity<Interaccion> getInteraccion(@PathVariable Integer id_interaccion){
        return ResponseEntity.ok(service.getInteraccion(id_interaccion));
    }

    @PostMapping
    public ResponseEntity<Interaccion> addInteraccion(@RequestBody Interaccion interaccion) {
        Interaccion nuevaInteraccion = service.addInteraccion(interaccion);
        return new ResponseEntity<>(nuevaInteraccion, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id_interaccion}")
    public ResponseEntity<Void> deleteInteraccion(@PathVariable Integer id_interaccion) {
        service.deleteInteraccion(id_interaccion);
        return ResponseEntity.noContent().build();
    }

    // Alternar me gusta (dar o quitar me gusta según el estado actual)
    @PostMapping("/toggle")
    public Interaccion toggleLike(@RequestParam Integer idUsuario, @RequestParam Integer idResenia) {
        return service.toggleLike(idUsuario, idResenia);
    }

    // Obtener información de me gusta de una reseña
    @GetMapping("/resenia/{idResenia}")
    public Map<String, Object> likesDeResenia(@PathVariable Integer idResenia,
                                             @RequestParam(required = false) Integer idUsuario) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("total", service.contarLikes(idResenia));
        resp.put("likeado", idUsuario != null && service.yaLikeado(idUsuario, idResenia));
        return resp;
    }
}
