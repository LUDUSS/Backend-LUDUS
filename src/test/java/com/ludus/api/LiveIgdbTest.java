package com.ludus.api;

import com.ludus.api.security.JwtFilter;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

// Opt-in: iniciar primero la aplicacion sin DDL; no requiere usuarios ni escrituras en MySQL.
@EnabledIfEnvironmentVariable(named = "LUDUS_LIVE_URL", matches = "http://localhost:[0-9]+")
class LiveIgdbTest {
    @Test void realSearchWithAndWithoutLudusJwt() throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        URI uri = URI.create(System.getenv("LUDUS_LIVE_URL") + "/ludus/igdb/buscar?nombre=Elden%20Ring");
        HttpResponse<String> anonymous = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(12)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(401, anonymous.statusCode());
        String token = Jwts.builder().subject("integration-probe@example.com")
                .expiration(new Date(System.currentTimeMillis()+60000)).signWith(JwtFilter.getSignInKey()).compact();
        HttpResponse<String> authenticated = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(12))
                .header("Authorization", "Bearer " + token).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, authenticated.statusCode(), "La consulta real autenticada debe obtener datos de IGDB");
        assertTrue(authenticated.body().contains("Elden Ring"));
        for (String endpoint : new String[]{"/ludus/igdb/juegos/119133", "/ludus/igdb/juegos?genreId=12&platformId=6&size=2"}) {
            HttpResponse<String> response=client.send(HttpRequest.newBuilder(URI.create(System.getenv("LUDUS_LIVE_URL")+endpoint))
                .timeout(Duration.ofSeconds(12)).header("Authorization","Bearer "+token).build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(200,response.statusCode(),"Lectura IGDB real: "+endpoint);
        }
    }
}
