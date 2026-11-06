package com.ludus.api;

import com.ludus.api.services.IgdbService;
import com.ludus.api.security.JwtFilter;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Date;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IgdbSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean IgdbService igdb;

    @Test void actualServletRegistrationRejectsAnonymousBeforeController() throws Exception {
        mvc.perform(get("/ludus/igdb/buscar").param("nombre", "Elden Ring"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(igdb);
    }
    @Test void signedLudusJwtReachesIgdbController() throws Exception {
        when(igdb.buscarJuegos("Elden Ring",0,10,"relevancia",null,null)).thenReturn(new com.ludus.api.dtos.CatalogDtos.Page<>(java.util.List.of(),0,10,false));
        String jwt = Jwts.builder().subject("test@example.com").expiration(new Date(System.currentTimeMillis()+60000))
                .signWith(JwtFilter.getSignInKey()).compact();
        mvc.perform(get("/ludus/igdb/buscar").param("nombre", "Elden Ring").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.items").isArray());
        verify(igdb).buscarJuegos("Elden Ring",0,10,"relevancia",null,null);
    }
    @Test void actualCorsPreflightDoesNotRequireJwt() throws Exception {
        mvc.perform(options("/ludus/igdb/buscar").header("Origin", "http://localhost:5502")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5502"));
        verifyNoInteractions(igdb);
    }
}
