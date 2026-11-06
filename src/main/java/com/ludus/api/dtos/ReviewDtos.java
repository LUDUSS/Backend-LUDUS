package com.ludus.api.dtos;
import java.time.LocalDateTime;
public final class ReviewDtos {
    private ReviewDtos() {}
    public record Input(String texto, Double calificacion) {}
    public record Author(Integer idUsuario, String nombre) {}
    public record Game(Integer localGameId, Long igdbId, String nombre, String portadaUrl) {}
    public record Review(Integer idResenia, String texto, Double calificacion, LocalDateTime fechaPublicacion,
                         Author autor, Game juego) {}
    public record Profile(Integer idUsuario, String nombre, String sobreMi, long cantidadResenias,
                          long juegosResenados, Double promedioCalificacion, long totalLikes) {}
}
