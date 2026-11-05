package com.ludus.api.controllers;

import com.ludus.api.models.Interaccion;
import com.ludus.api.services.InteraccionesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/ludus/interaccion/")
@CrossOrigin(origins = "*")
public class InteraccionController {

    private final InteraccionesService service;

    @Autowired
    public InteraccionController(InteraccionesService service) {
        this.service = service;
    }

    @GetMapping
    public List<Interaccion> getInteracciones() {
        return service.getInteracciones();
    }

    @GetMapping("/{id_interaccion}")
    public Interaccion getInteraccion(@PathVariable Integer id_interaccion) {
        return service.getInteraccion(id_interaccion);
    }

    @PostMapping
    public Interaccion addInteraccion(@RequestBody Interaccion interaccion) {
        return service.addInteraccion(interaccion);
    }

    @DeleteMapping("/{id_interaccion}")
    public Interaccion deleteInteraccion(@PathVariable Integer id_interaccion) {
        return service.deleteInteraccion(id_interaccion);
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
