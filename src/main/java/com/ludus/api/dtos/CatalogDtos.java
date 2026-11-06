package com.ludus.api.dtos;
import java.util.List;
public final class CatalogDtos {
    private CatalogDtos() {}
    public record Page<T>(List<T> items, int page, int size, boolean hasNext) {}
    public record Lookup(Long id, String nombre) {}
    public record Social(Double promedio, long cantidad) {}
    public record Game(Long igdbId, Integer localGameId, String nombre, String descripcion,
                       String portadaUrl, List<Lookup> generos, List<Lookup> plataformas,
                       Long lanzamiento, Double ratingIgdb, String slug, List<String> screenshots,
                       Social ratingLudus) {}
}
