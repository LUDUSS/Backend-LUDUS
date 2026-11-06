package com.ludus.api.igdb;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown=true)
public record IgdbGame(Long id, String name, String summary, List<Named> genres, List<Named> platforms,
                       Image cover, Long first_release_date, Double rating, String slug, List<Image> screenshots) {
    @JsonIgnoreProperties(ignoreUnknown=true) public record Named(Long id, String name) {}
    @JsonIgnoreProperties(ignoreUnknown=true) public record Image(String image_id) {}
}
